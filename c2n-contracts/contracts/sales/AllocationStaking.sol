//SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.0;

import "@openzeppelin/contracts/token/ERC20/IERC20.sol";
import "@openzeppelin/contracts/token/ERC20/utils/SafeERC20.sol";
import "@openzeppelin/contracts-upgradeable/access/OwnableUpgradeable.sol";
import "../interfaces/ISalesFactory.sol";
import "@openzeppelin/contracts/utils/Context.sol";

contract AllocationStaking is OwnableUpgradeable{

    using SafeERC20 for IERC20;

    // Info of each user.
    struct UserInfo {
        uint256 amount;     // How many LP tokens the user has provided.
        uint256 rewardDebt; // Reward debt. Current reward debt when user joined farm. See explanation below.
        //
        // We do some fancy math here. Basically, any point in time, the amount of ERC20s
        // entitled to a user but is pending to be distributed is:
        //
        //   pending reward = (user.amount * pool.accERC20PerShare) - user.rewardDebt
        //
        // Whenever a user deposits or withdraws LP tokens to a pool. Here's what happens:
        //   1. The pool's `accERC20PerShare` (and `lastRewardBlock`) gets updated.
        //   2. User receives the pending reward sent to his/her address.
        //   3. User's `amount` gets updated.
        //   4. User's `rewardDebt` gets updated.
        // 代币解锁事件，用户参与代币购买，则会锁定用户质押的代币
        uint256 tokensUnlockTime; // If user registered for sale, returns when tokens are getting unlocked
        // 用户注册过的销售方案
        address [] salesRegistered;
    }

    // Info of each pool.
    struct PoolInfo {
        IERC20 lpToken;             // Address of LP token contract.
        uint256 allocPoint;         // How many allocation points assigned to this pool. ERC20s to distribute per block.
        uint256 lastRewardTimestamp;    // Last timstamp that ERC20s distribution occurs.
        uint256 accERC20PerShare;   // Accumulated ERC20s per share, times 1e36.
        uint256 totalDeposits; // Total amount of tokens deposited at the moment (staked)
    }


    // Address of the ERC20 Token contract.
    IERC20 public erc20;
    // The total amount of ERC20 that's paid out as reward.
    uint256 public paidOut;
    // ERC20 tokens rewarded per second.
    uint256 public rewardPerSecond;
    // Total rewards added to farm
    uint256 public totalRewards;
    // Address of sales factory contract
    ISalesFactory public salesFactory;
    // Info of each pool.
    PoolInfo[] public poolInfo;
    // Info of each user that stakes LP tokens.
    mapping(uint256 => mapping(address => UserInfo)) public userInfo;
    // Total allocation points. Must be the sum of all allocation points in all pools.
    uint256 public totalAllocPoint;

    // The timestamp when farming starts.
    uint256 public startTimestamp;
    // The timestamp when farming ends.
    uint256 public endTimestamp;

    // Events
    event Deposit(address indexed user, uint256 indexed pid, uint256 amount);
    event Withdraw(address indexed user, uint256 indexed pid, uint256 amount);
    event EmergencyWithdraw(address indexed user, uint256 indexed pid, uint256 amount);
    event CompoundedEarnings(address indexed user, uint256 indexed pid, uint256 amountAdded, uint256 totalDeposited);

    // Restricting calls to only verified sales
    modifier onlyVerifiedSales {
        // 校验调用者地址是 saleFactory 工厂创建的
        require(salesFactory.isSaleCreatedThroughFactory(msg.sender), "Sale not created through factory.");
        _;
    }
    // 相比Farm合约，allocation 没有构造函数，而是使用的initialize来替代
    // 因为allocationStaking使用的是代理模式, 代理模式会调用initialize来进行初始化
    function initialize(
        IERC20 _erc20,
        uint256 _rewardPerSecond,
        uint256 _startTimestamp,
        address _salesFactory
    ) initializer
    public
    {
        __Ownable_init(_msgSender());

        erc20 = _erc20;
        rewardPerSecond = _rewardPerSecond;
        startTimestamp = _startTimestamp;
        endTimestamp = _startTimestamp;
        // Create sales factory contract
        salesFactory = ISalesFactory(_salesFactory);
    }

    // Function where owner can set sales factory in case of upgrading some of smart-contracts
    function setSalesFactory(address _salesFactory) external onlyOwner {
        require(_salesFactory != address(0));
        salesFactory = ISalesFactory(_salesFactory);
    }

    // Number of LP pools
    function poolLength() external view returns (uint256) {
        return poolInfo.length;
    }

    // Fund the farm, increase the end block
    function fund(uint256 _amount) public {
        require(block.timestamp < endTimestamp, "fund: too late, the farm is closed");
        erc20.safeTransferFrom(address(msg.sender), address(this), _amount);
        endTimestamp += _amount/rewardPerSecond;
        totalRewards = totalRewards+_amount;
    }

    // Add a new lp to the pool. Can only be called by the owner.
    // DO NOT add the same LP token more than once. Rewards will be messed up if you do.
    function add(uint256 _allocPoint, IERC20 _lpToken, bool _withUpdate) public onlyOwner {
        if (_withUpdate) {
            massUpdatePools();
        }
        uint256 lastRewardTimestamp = block.timestamp > startTimestamp ? block.timestamp : startTimestamp;
        totalAllocPoint = totalAllocPoint+_allocPoint;
        // Push new PoolInfo
        poolInfo.push(
            PoolInfo({
        lpToken : _lpToken,
        allocPoint : _allocPoint,
        lastRewardTimestamp : lastRewardTimestamp,
        accERC20PerShare : 0,
        totalDeposits : 0
        })
        );
    }

    // Update the given pool's ERC20 allocation point. Can only be called by the owner.
    function set(uint256 _pid, uint256 _allocPoint, bool _withUpdate) public onlyOwner {
        if (_withUpdate) {
            massUpdatePools();
        }
        totalAllocPoint = totalAllocPoint-poolInfo[_pid].allocPoint+_allocPoint;
        poolInfo[_pid].allocPoint = _allocPoint;
    }

    // View function to see deposited LP for a user.
    function deposited(uint256 _pid, address _user) public view returns (uint256) {
        UserInfo storage user = userInfo[_pid][_user];
        return user.amount;
    }

    // View function to see pending ERC20s for a user.
    function pending(uint256 _pid, address _user) public view returns (uint256) {
        PoolInfo storage pool = poolInfo[_pid];
        UserInfo storage user = userInfo[_pid][_user];
        uint256 accERC20PerShare = pool.accERC20PerShare;

        uint256 lpSupply = pool.totalDeposits;

        // Compute pending ERC20s
        if (block.timestamp > pool.lastRewardTimestamp && lpSupply != 0) {
            uint256 lastTimestamp = block.timestamp < endTimestamp ? block.timestamp : endTimestamp;
            uint256 nrOfSeconds = lastTimestamp-pool.lastRewardTimestamp;
            uint256 erc20Reward = nrOfSeconds*rewardPerSecond*pool.allocPoint/totalAllocPoint;
            accERC20PerShare = accERC20PerShare+(erc20Reward*1e36/lpSupply);
        }
        return user.amount*accERC20PerShare/1e36-user.rewardDebt;
    }

    // View function for total reward the farm has yet to pay out.
    // NOTE: this is not necessarily the sum of all pending sums on all pools and users
    //      example 1: when tokens have been wiped by emergency withdraw
    //      example 2: when one pool has no LP supply
    function totalPending() external view returns (uint256) {
        if (block.timestamp <= startTimestamp) {
            return 0;
        }

        uint256 lastTimestamp = block.timestamp < endTimestamp ? block.timestamp : endTimestamp;
        return rewardPerSecond*(lastTimestamp - startTimestamp)-paidOut;
    }

    // Update reward variables for all pools. Be careful of gas spending!
    function massUpdatePools() public {
        uint256 length = poolInfo.length;
        for (uint256 pid = 0; pid < length; ++pid) {
            updatePool(pid);
        }
    }

    function setTokensUnlockTime(uint256 _pid, address _user, uint256 _tokensUnlockTime) external onlyVerifiedSales {
        UserInfo storage user = userInfo[_pid][_user];
        // Require that tokens are currently unlocked
        require(user.tokensUnlockTime <= block.timestamp);
        user.tokensUnlockTime = _tokensUnlockTime;
        // Add sale to the array of sales user registered for.
        user.salesRegistered.push(msg.sender);
    }

    // Update reward variables of the given pool to be up-to-date.
    function updatePool(uint256 _pid) public {
        PoolInfo storage pool = poolInfo[_pid];

        uint256 lastTimestamp = block.timestamp < endTimestamp ? block.timestamp : endTimestamp;

        if (lastTimestamp <= pool.lastRewardTimestamp) {
            lastTimestamp = pool.lastRewardTimestamp;
        }

        uint256 lpSupply = pool.totalDeposits;

        if (lpSupply == 0) {
            pool.lastRewardTimestamp = lastTimestamp;
            return;
        }

        uint256 nrOfSeconds = lastTimestamp-pool.lastRewardTimestamp;
        uint256 erc20Reward = nrOfSeconds*rewardPerSecond*pool.allocPoint/totalAllocPoint;

        // Update pool accERC20PerShare
        pool.accERC20PerShare = pool.accERC20PerShare+erc20Reward*1e36/lpSupply;

        // Update pool lastRewardTimestamp
        pool.lastRewardTimestamp = lastTimestamp;
    }

    // Deposit LP tokens to Farm for ERC20 allocation.
    function deposit(uint256 _pid, uint256 _amount) public {
        PoolInfo storage pool = poolInfo[_pid];
        UserInfo storage user = userInfo[_pid][msg.sender];

        uint256 depositAmount = _amount;

        // Update pool
        updatePool(_pid);

        // Transfer pending amount to user if already staking
        if (user.amount > 0) {
            uint256 pendingAmount = user.amount*pool.accERC20PerShare/1e36-user.rewardDebt;
            erc20Transfer(msg.sender, pendingAmount);
        }

        // Safe transfer lpToken from user
        pool.lpToken.safeTransferFrom(address(msg.sender), address(this), _amount);
        // Add deposit to total deposits
        pool.totalDeposits = pool.totalDeposits+depositAmount;
        // Add deposit to user's amount
        user.amount = user.amount+depositAmount;
        // Compute reward debt
        user.rewardDebt = user.amount*pool.accERC20PerShare/1e36;
        // Emit relevant event
        emit Deposit(msg.sender, _pid, depositAmount);
    }

    // Withdraw LP tokens from Farm.
    function withdraw(uint256 _pid, uint256 _amount) public {
        PoolInfo storage pool = poolInfo[_pid];
        UserInfo storage user = userInfo[_pid][msg.sender];

        require(user.tokensUnlockTime <= block.timestamp, "Last sale you registered for is not finished yet.");
        require(user.amount >= _amount, "withdraw: can't withdraw more than deposit");

        // Update pool
        updatePool(_pid);

        // Compute user's pending amount
        uint256 pendingAmount = user.amount*pool.accERC20PerShare/1e36-user.rewardDebt;

        // Transfer pending amount to user
        erc20Transfer(msg.sender, pendingAmount);
        user.amount = user.amount-_amount;
        user.rewardDebt = user.amount*pool.accERC20PerShare/1e36;

        // Transfer withdrawal amount to user
        pool.lpToken.safeTransfer(address(msg.sender), _amount);
        pool.totalDeposits = pool.totalDeposits-_amount;

        if (_amount > 0) {
            // Reset the tokens unlock time
            user.tokensUnlockTime = 0;
        }

        // Emit relevant event
        emit Withdraw(msg.sender, _pid, _amount);
    }

    // Function to compound earnings into deposit
    function compound(uint256 _pid) public {
        PoolInfo storage pool = poolInfo[_pid];
        UserInfo storage user = userInfo[_pid][msg.sender];

        require(user.amount >= 0, "User does not have anything staked.");

        // Update pool
        updatePool(_pid);

        uint256 pendingAmount = user.amount*pool.accERC20PerShare/1e36-user.rewardDebt;

        // Increase amount user is staking
        user.amount = user.amount+pendingAmount;
        user.rewardDebt = user.amount*pool.accERC20PerShare/1e36;

        // Increase pool's total deposits
        pool.totalDeposits = pool.totalDeposits+pendingAmount;
        emit CompoundedEarnings(msg.sender, _pid, pendingAmount, user.amount);
    }

    // Withdraw without caring about rewards. EMERGENCY ONLY.
    function emergencyWithdraw(uint256 _pid) public {
        PoolInfo storage pool = poolInfo[_pid];
        UserInfo storage user = userInfo[_pid][msg.sender];
        require(user.tokensUnlockTime <= block.timestamp,
            "Emergency withdraw blocked during sale and cooldown period.");

        // Perform safeTransfer
        pool.lpToken.safeTransfer(address(msg.sender), user.amount);
        emit EmergencyWithdraw(msg.sender, _pid, user.amount);
        // Adapt contract states
        pool.totalDeposits = pool.totalDeposits-user.amount;
        user.amount = 0;
        user.rewardDebt = 0;
        user.tokensUnlockTime = 0;
    }

    // Transfer ERC20 and update the required ERC20 to payout all rewards
    function erc20Transfer(address _to, uint256 _amount) internal {
        erc20.transfer(_to, _amount);
        paidOut += _amount;
    }

    // Function to fetch deposits and earnings at one call for multiple users for passed pool id.
    function getPendingAndDepositedForUsers(address [] memory users, uint pid)
    external
    view
    returns (uint256 [] memory, uint256 [] memory)
    {
        uint256 [] memory deposits = new uint256[](users.length);
        uint256 [] memory earnings = new uint256[](users.length);

        // Get deposits and earnings for selected users
        for (uint i = 0; i < users.length; i++) {
            deposits[i] = deposited(pid, users[i]);
            earnings[i] = pending(pid, users[i]);
        }

        return (deposits, earnings);
    }


}
