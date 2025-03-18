//SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.0;

import "../interfaces/IAdmin.sol";
import "../interfaces/ISalesFactory.sol";
import "../interfaces/IAllocationStaking.sol";
import "@openzeppelin/contracts/utils/cryptography/ECDSA.sol";
import "@openzeppelin/contracts/token/ERC20/utils/SafeERC20.sol";
import "@openzeppelin/contracts/interfaces/IERC20Metadata.sol";
import "@openzeppelin/contracts/utils/cryptography/MessageHashUtils.sol";

contract C2NSale {
    using ECDSA for bytes32;
    using MessageHashUtils for bytes32;
    using SafeERC20 for IERC20;

    // Pointer to Allocation staking contract
    IAllocationStaking public allocationStakingContract;
    // Pointer to sales factory contract
    ISalesFactory public factory;
    // Admin contract
    IAdmin public admin;

    struct Sale {
        // 代币地址
        IERC20 token;
        // Is sale created 售卖已创建
        bool isCreated;
        // Are earnings withdrawn 项目方是否已经提取了eth
        bool earningsWithdrawn;
        // Is leftover withdrawn 项目方是否已经取回剩余未售出代币
        bool leftoverWithdrawn;
        // Have tokens been deposited 代币生成事件已经发生
        bool tokensDeposited;

        // Address of sale owner 项目方地址
        address saleOwner;
        // Price of the token quoted in ETH 项目币单价
        uint256 tokenPriceInETH;
        // Amount of tokens to sell 即将售卖项目币数量
        uint256 amountOfTokensToSell;
        // Total tokens being sold 已售出项目币数量
        uint256 totalTokensSold;
        // Total ETH Raised 获得的eth
        uint256 totalETHRaised;
        // Sale start time 售卖开始时间
        uint256 saleStart;
        // Sale end time 售卖结束时间
        uint256 saleEnd;
        // When tokens can be withdrawn 代币可以提取时间
        uint256 tokensUnlockTime;
        // maxParticipation 最大参与投资人数量    
        uint256 maxParticipation;
    }

    // Participation structure 每一个投资人的购买属性
    struct Participation {
        // 购买代币数量
        uint256 amountBought;
        // 支付的eth数量
        uint256 amountETHPaid;
        // 售卖参与时间
        uint256 timeParticipated;
        // 分批是否提取，每一个时间分片上是否提取了代币
        bool[] isPortionWithdrawn;
    }

    // 注册的基础属性
    struct Registration {
        uint256 registrationTimeStarts;
        uint256 registrationTimeEnds;
        uint256 numberOfRegistrants;
    }

    // Sale 售卖信息 全局唯一
    Sale public sale;
    // Registration 注册信息 全局唯一 
    Registration public registration;
    // Number of users participated in the sale.
    uint256 public numberOfParticipants;
    // Mapping user to his participation 投资人购买信息
    mapping(address => Participation) public userToParticipation;
    // Mapping if user is registered or not
    mapping(address => bool) public isRegistered;
    // mapping if user is participated or not
    mapping(address => bool) public isParticipated;
    // Times when portions are getting unlocked 分批解锁计划，计划结束之后，投资者不同的时间段之内，投资者可以提取的比例
    uint256[] public vestingPortionsUnlockTime;
    // Percent of the participation user can withdraw
    // 每一个时间分片上可以提取的比例
    uint256[] public vestingPercentPerPortion;
    //Precision for percent for portion vesting
    // 总的分发比例
    uint256 public portionVestingPrecision;
    // Max vesting time shift
    uint256 public maxVestingTimeShift;

    // Restricting calls only to sale owner 权限管理
    modifier onlySaleOwner() {
        require(msg.sender == sale.saleOwner, "OnlySaleOwner:: Restricted");
        _;
    }

    modifier onlyAdmin() {
        require(
            admin.isAdmin(msg.sender),
            "Only admin can call this function."
        );
        _;
    }

    // Events
    event TokensSold(address user, uint256 amount);
    event UserRegistered(address user);
    event TokenPriceSet(uint256 newPrice);
    event MaxParticipationSet(uint256 maxParticipation);
    event TokensWithdrawn(address user, uint256 amount);
    event SaleCreated(
        address saleOwner,
        uint256 tokenPriceInETH,
        uint256 amountOfTokensToSell,
        uint256 saleEnd
    );
    event StartTimeSet(uint256 startTime);
    event RegistrationTimeSet(
        uint256 registrationTimeStarts,
        uint256 registrationTimeEnds
    );

    // Constructor, always initialized through SalesFactory
    // 管理员和分配质押的协议
    constructor(address _admin, address _allocationStaking) {
        require(_admin != address(0));
        require(_allocationStaking != address(0));
        admin = IAdmin(_admin);
        factory = ISalesFactory(msg.sender);
        allocationStakingContract = IAllocationStaking(_allocationStaking);
    }

    /// @notice         Function to set vesting params 设置分发计划
    // 代币解锁之后，不同的时间段内，可以分别提取的代币数量
    function setVestingParams(
            uint256[] memory _unlockingTimes,
            uint256[] memory _percents,
            uint256 _maxVestingTimeShift
        ) 
        external onlyAdmin 
    {
        require(
            vestingPercentPerPortion.length == 0 &&
            vestingPortionsUnlockTime.length == 0
        );
        require(_unlockingTimes.length == _percents.length);
        require(portionVestingPrecision > 0, "Safeguard for making sure setSaleParams get first called.");
        require(_maxVestingTimeShift <= 30 days, "Maximal shift is 30 days.");

        // Set max vesting time shift
        maxVestingTimeShift = _maxVestingTimeShift;

        uint256 sum;

        for (uint256 i = 0; i < _unlockingTimes.length; i++) {
            vestingPortionsUnlockTime.push(_unlockingTimes[i]);
            vestingPercentPerPortion.push(_percents[i]);
            sum += _percents[i];
        }

        require(sum == portionVestingPrecision, "Percent distribution issue.");
    }
    /**
    设置解锁时间的偏移量
    */ 
    function shiftVestingUnlockingTimes(uint256 timeToShift)
        external
        onlyAdmin
    {
        require(
            timeToShift > 0 && timeToShift < maxVestingTimeShift,
            "Shift must be nonzero and smaller than maxVestingTimeShift."
        );

        // Time can be shifted only once.
        maxVestingTimeShift = 0;

        for (uint256 i = 0; i < vestingPortionsUnlockTime.length; i++) {
            vestingPortionsUnlockTime[i] = vestingPortionsUnlockTime[i]+ timeToShift;
        }
    }

    /// @notice     Admin function to set sale parameters
     /**
    设置销售基础的属性
    */ 
    function setSaleParams(
        address _token,
        address _saleOwner,
        uint256 _tokenPriceInETH,
        uint256 _amountOfTokensToSell,
        uint256 _saleEnd,
        uint256 _tokensUnlockTime,
        uint256 _portionVestingPrecision,
        uint256 _maxParticipation
    ) external onlyAdmin 
    {
        require(!sale.isCreated, "setSaleParams: Sale is already created.");
        require(
            _saleOwner != address(0),
            "setSaleParams: Sale owner address can not be 0."
        );
        require(
            _tokenPriceInETH != 0 &&
            _amountOfTokensToSell != 0 &&
            _saleEnd > block.timestamp &&
            _tokensUnlockTime > block.timestamp &&
            _maxParticipation > 0,
            "setSaleParams: Bad input"
        );
        require(_portionVestingPrecision >= 100, "Should be at least 100");

        // Set params
        sale.token = IERC20(_token);
        sale.isCreated = true;
        sale.saleOwner = _saleOwner;
        sale.tokenPriceInETH = _tokenPriceInETH;
        sale.amountOfTokensToSell = _amountOfTokensToSell;
        sale.saleEnd = _saleEnd;
        sale.tokensUnlockTime = _tokensUnlockTime;
        sale.maxParticipation = _maxParticipation;

        // Set portion vesting precision
        portionVestingPrecision = _portionVestingPrecision;
        // Emit event
        emit SaleCreated(
            sale.saleOwner,
            sale.tokenPriceInETH,
            sale.amountOfTokensToSell,
            sale.saleEnd
        );
    }

    // @notice     Function to retroactively set sale token address, can be called only once,
    //             after initial contract creation has passed. Added as an options for teams which
    //             are not having token at the moment of sale launch.
      /**
    设置售卖的币种
    */ 
    function setSaleToken(address saleToken)
        external
        onlyAdmin
    {
        require(address(sale.token) == address(0));
        sale.token = IERC20(saleToken);
    }


    /// @notice     Function to set registration period parameters
      /**
    设置投资者注册时间
    */ 
    function setRegistrationTime(uint256 _registrationTimeStarts,uint256 _registrationTimeEnds) 
        external onlyAdmin 
    {
        require(sale.isCreated);
        require(registration.registrationTimeStarts == 0);
        require(
            _registrationTimeStarts >= block.timestamp &&
            _registrationTimeEnds > _registrationTimeStarts
        );
        require(_registrationTimeEnds < sale.saleEnd);

        if (sale.saleStart > 0) {
            require(_registrationTimeEnds < sale.saleStart, "registrationTimeEnds >= sale.saleStart is not allowed");
        }

        registration.registrationTimeStarts = _registrationTimeStarts;
        registration.registrationTimeEnds = _registrationTimeEnds;

        emit RegistrationTimeSet(
            registration.registrationTimeStarts,
            registration.registrationTimeEnds
        );
    }

    /**
    设置售卖开始时间
    */ 
    function setSaleStart(uint256 starTime) 
        external onlyAdmin 
    {
        require(sale.isCreated, "sale is not created.");
        require(sale.saleStart == 0, "setSaleStart: starTime is set already.");
        require(starTime > registration.registrationTimeEnds, "start time should greater than registrationTimeEnds.");
        require(starTime < sale.saleEnd, "start time should less than saleEnd time");
        require(starTime >= block.timestamp, "start time should be in the future.");
        sale.saleStart = starTime;

        // Fire event
        emit StartTimeSet(sale.saleStart);
    }

    /// @notice     Registration for sale.
    /// @param      signature is the message signed by the backend
    /**
    重要方法
    投资者注册销售方案的方法
    checkRegistrationSignature 方法，投资者注册对应的投资方案的时候，需要得到链下管理员的加签
    通常配合KYC过程进行，在本工程中只是简单的演示这个过程，通过线下进行加签，链上验签
    投资者需要选择自己购买的销售方案，管理员需要把这一次他的销售行为进行认可，为了确保认可不被篡改，就需要进行加签。
    链上验签，得到的就是恢复出来的加签用户，一定要等于当前管理员，这样保证了签名没有被篡改。
    */ 
    function registerForSale(bytes memory signature, uint256 pid)
        external
    {
        require(
            block.timestamp >= registration.registrationTimeStarts &&
            block.timestamp <= registration.registrationTimeEnds,
            "Registration gate is closed."
        );
        require(
            checkRegistrationSignature(signature, msg.sender),
            "Invalid signature"
        );
        require(
            !isRegistered[msg.sender],
            "User can not register twice."
        );
        isRegistered[msg.sender] = true;

        // Lock users stake
        allocationStakingContract.setTokensUnlockTime(
            pid,
            msg.sender,
            sale.saleEnd
        );

        // Increment number of registered users
        registration.numberOfRegistrants++;
        // Emit Registration event
        emit UserRegistered(msg.sender);
    }

    /// @notice     Admin function, to update token price before sale to match the closest $ desired rate.
    /// @dev        This will be updated with an oracle during the sale every N minutes, so the users will always
    ///             pay initialy set $ value of the token. This is to reduce reliance on the ETH volatility.
    function updateTokenPriceInETH(uint256 price) external onlyAdmin 
    {
        require(price > 0, "Price can not be 0.");
        // Allowing oracle to run and change the sale value
        sale.tokenPriceInETH = price;
        emit TokenPriceSet(price);
    }

    /// @notice     Admin function to postpone the sale
    function postponeSale(uint256 timeToShift) 
        external onlyAdmin 
    {
        require(
            block.timestamp < sale.saleStart,
            "sale already started."
        );
        //  postpone registration start time
        sale.saleStart = sale.saleStart+timeToShift;
        require(
            sale.saleStart + timeToShift < sale.saleEnd,
            "Start time can not be greater than end time."
        );
    }

    /// @notice     Function to extend registration period
    function extendRegistrationPeriod(uint256 timeToAdd) 
        external onlyAdmin 
    {
        require(
            registration.registrationTimeEnds+timeToAdd <
            sale.saleStart,
            "Registration period overflows sale start."
        );

        registration.registrationTimeEnds = registration.registrationTimeEnds+timeToAdd;
    }

    /// @notice     Admin function to set max participation before sale start
    function setCap(uint256 cap)
        external onlyAdmin
    {
        require(
            block.timestamp < sale.saleStart,
            "sale already started."
        );

        require(cap > 0, "Can't set max participation to 0");

        sale.maxParticipation = cap;

        emit MaxParticipationSet(sale.maxParticipation);
    }

    // Function for owner to deposit tokens, can be called only once.
    /**
    项目方存入代币，代币生成事件
    */ 
    function depositTokens() 
        external onlySaleOwner 
    {
        require(
            !sale.tokensDeposited, "Deposit can be done only once"
        );

        sale.tokensDeposited = true;

        sale.token.safeTransferFrom(
            msg.sender,
            address(this),
            sale.amountOfTokensToSell
        );
    }

    // Function to participate in the sales
    /**
    投资方参与购买代币的过程
    */ 
    function participate(bytes memory signature,uint256 amount) 
        external payable 
    {

        require(
            amount <= sale.maxParticipation,
            "Overflowing maximal participation for sale."
        );

        // User must have registered for the round in advance
        
        require(
            isRegistered[msg.sender],
            "Not registered for this sale."
        );

        // Verify the signature
        // 需要管理员在线下进行加签
        require(
            checkParticipationSignature(
                signature,
                msg.sender,
                amount
            ),
            "Invalid signature. Verification failed"
        );

        // Verify the timestamp
        require(
            block.timestamp >= sale.saleStart &&
            block.timestamp < sale.saleEnd, "sale didn't start or it's ended."
        );

        // Check user haven't participated before
        require(!isParticipated[msg.sender], "User can participate only once.");

        // Disallow contract calls.
        require(msg.sender == tx.origin, "Only direct contract calls.");

        // Compute the amount of tokens user is buying
        uint256 amountOfTokensBuying =
        (msg.value)*uint(10) ** IERC20Metadata(address(sale.token)).decimals()/sale.tokenPriceInETH;

        // Must buy more than 0 tokens
        require(amountOfTokensBuying > 0, "Can't buy 0 tokens");

        // Check in terms of user allo
        require(
            amountOfTokensBuying <= amount,
            "Trying to buy more than allowed."
        );

        // Increase amount of sold tokens
        sale.totalTokensSold = sale.totalTokensSold+amountOfTokensBuying;

        // Increase amount of ETH raised
        sale.totalETHRaised = sale.totalETHRaised+msg.value;

        bool[] memory _isPortionWithdrawn = new bool[](
            vestingPortionsUnlockTime.length
        );

        // Create participation object
        Participation memory p = Participation({
        amountBought : amountOfTokensBuying,
        amountETHPaid : msg.value,
        timeParticipated : block.timestamp,
        isPortionWithdrawn : _isPortionWithdrawn
        });

        // Add participation for user.
        userToParticipation[msg.sender] = p;
        // Mark user is participated
        isParticipated[msg.sender] = true;
        // Increment number of participants in the Sale.
        numberOfParticipants++;

        emit TokensSold(msg.sender, amountOfTokensBuying);
    }

    /// Users can claim their participation
    /**
    投资人代币生成解锁之后可以分批次的提取代币
    */ 
    function withdrawTokens(uint256 portionId) external 
    {
        require(
            block.timestamp >= sale.tokensUnlockTime,
            "Tokens can not be withdrawn yet."
        );
        require(
            portionId < vestingPercentPerPortion.length,
            "Portion id out of range."
        );

        Participation storage p = userToParticipation[msg.sender];

        if (!p.isPortionWithdrawn[portionId] &&
            vestingPortionsUnlockTime[portionId] <= block.timestamp) 
        {
            p.isPortionWithdrawn[portionId] = true;
            /**
            根据比例计算可以提取的代币数量
            例如 [100, 200, 200] 第一批次就是 100/500, 第二批次就是 200/500, 第三批次就是 200/500
            这样能控制我们分批次提取代币的速度
            */ 
            uint256 amountWithdrawing = p.amountBought * vestingPercentPerPortion[portionId] / portionVestingPrecision;

            // Withdraw percent which is unlocked at that portion
            if (amountWithdrawing > 0) {
                sale.token.safeTransfer(msg.sender, amountWithdrawing);
                emit TokensWithdrawn(msg.sender, amountWithdrawing);
            }
        } else {
            revert("Tokens already withdrawn or portion not unlocked yet.");
        }
    }

    // Expose function where user can withdraw multiple unlocked portions at once.
    /**
    如果可以批量提取，则使用for循环提取
    一共三个阶段[100, 200, 200]
    假设当前时间过了第二个阶段了, 第一个批次100， 第二个批次200，就可以一次性提取出来
    */ 
    function withdrawMultiplePortions(uint256 [] calldata portionIds) 
        external 
    {
        uint256 totalToWithdraw = 0;

        Participation storage p = userToParticipation[msg.sender];

        for (uint i = 0; i < portionIds.length; i++) {
            uint256 portionId = portionIds[i];
            require(portionId < vestingPercentPerPortion.length);

            if (
                !p.isPortionWithdrawn[portionId] &&
            vestingPortionsUnlockTime[portionId] <= block.timestamp
            ) {
                p.isPortionWithdrawn[portionId] = true;
                uint256 amountWithdrawing = p.amountBought*vestingPercentPerPortion[portionId]/portionVestingPrecision;
                // Withdraw percent which is unlocked at that portion
                totalToWithdraw = totalToWithdraw+amountWithdrawing;
            }
        }

        if (totalToWithdraw > 0) {
            sale.token.safeTransfer(msg.sender, totalToWithdraw);
            emit TokensWithdrawn(msg.sender, totalToWithdraw);
        }
    }

    // Internal function to handle safe transfer
    function safeTransferETH(address to, uint256 value) internal {
        (bool success,) = to.call{value : value}(new bytes(0));
        require(success);
    }

    /// Function to withdraw all the earnings and the leftover of the sale contract.
    /**
    项目方提取所有收益和剩余的代币
    */ 
    function withdrawEarningsAndLeftover() external onlySaleOwner {
        withdrawEarningsInternal();
        withdrawLeftoverInternal();
    }

    // Function to withdraw only earnings
    function withdrawEarnings() external onlySaleOwner {
        withdrawEarningsInternal();
    }

    // Function to withdraw only leftover
    function withdrawLeftover() external onlySaleOwner {
        withdrawLeftoverInternal();
    }

    // function to withdraw earnings
    function withdrawEarningsInternal() internal {
        // Make sure sale ended
        require(block.timestamp >= sale.saleEnd, "sale is not ended yet.");

        // Make sure owner can't withdraw twice
        require(!sale.earningsWithdrawn, "owner can't withdraw earnings twice");
        sale.earningsWithdrawn = true;
        // Earnings amount of the owner in ETH
        uint256 totalProfit = sale.totalETHRaised;

        safeTransferETH(msg.sender, totalProfit);
    }

    // Function to withdraw leftover
    function withdrawLeftoverInternal() internal {
        // Make sure sale ended
        require(block.timestamp >= sale.saleEnd, "sale is not ended yet.");

        // Make sure owner can't withdraw twice
        require(!sale.leftoverWithdrawn, "owner can't withdraw leftover twice");
        sale.leftoverWithdrawn = true;

        // Amount of tokens which are not sold
        uint256 leftover = sale.amountOfTokensToSell-sale.totalTokensSold;

        if (leftover > 0) {
            sale.token.safeTransfer(msg.sender, leftover);
        }
    }

    /// @notice     Check signature user submits for registration.
    /// @param      signature is the message signed by the trusted entity (backend)
    /// @param      user is the address of user which is registering for sale
    /**
    验证投资人注册销售方案的签名
    */ 
    function checkRegistrationSignature(bytes memory signature, address user) 
        public view returns (bool) 
    {
        bytes32 hash = keccak256(
            abi.encodePacked(user, address(this))
        );
        bytes32 messageHash = hash.toEthSignedMessageHash();
        return admin.isAdmin(messageHash.recover(signature));
    }

    // Function to check if admin was the message signer
    function checkParticipationSignature(bytes memory signature,address user,uint256 amount) 
        public view returns (bool) 
    {
        return admin.isAdmin(
            getParticipationSigner(signature, user,amount)
        );
        
    }

    /// @notice     Check who signed the message
    /// @param      signature is the message allowing user to participate in sale
    /// @param      user is the address of user for which we're signing the message
    /// @param      amount is the maximal amount of tokens user can buy
    function getParticipationSigner(bytes memory signature, address user, uint256 amount) 
        public view returns (address) 
    {
        bytes32 hash = keccak256(
            abi.encodePacked(
                user,
                amount,
                address(this)
            )
        );
        bytes32 messageHash = hash.toEthSignedMessageHash();
        return messageHash.recover(signature);
    }

    /// @notice     Function to get participation for passed user address
    function getParticipation(address _user)
        external
        view
    returns (uint256, uint256, uint256,bool[] memory)
    {
        Participation memory p = userToParticipation[_user];
        return (
            p.amountBought,
            p.amountETHPaid,
            p.timeParticipated,
            p.isPortionWithdrawn
        );
    }

    /// @notice     Function to get number of registered users for sale
    function getNumberOfRegisteredUsers() 
        external view returns (uint256) 
    {
        return registration.numberOfRegistrants;
    }

    /// @notice     Function to get all info about vesting.
    function getVestingInfo()
        external view
        returns (uint256[] memory, uint256[] memory)
    {
        return (vestingPortionsUnlockTime, vestingPercentPerPortion);
    }

    // Function to act as a fallback and handle receiving ETH.
    receive() external payable {

    }
}
