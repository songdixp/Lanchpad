// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

import "@openzeppelin/contracts/token/ERC20/IERC20.sol";
import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/token/ERC20/utils/SafeERC20.sol";

// Farm distributes the ERC20 rewards based on staked LP to each user.
//
// Cloned from https://github.com/SashimiProject/sashimiswap/blob/master/contracts/MasterChef.sol
// Modified by LTO Network to work for non-mintable ERC20.

contract FarmingC2N is Ownable {

    using SafeERC20 for IERC20;

    // 运行态用户质押信息
    struct UserInfo {
        uint256 amount;     // 用户提供的LP的数量
        uint256 rewardDebt; // 用户质押总奖励数
        // 中间变量
        /*
        严格意义上讲不是用户真正历史获取的代币数，而是用来存储 userRewardPerTokenPaid 用户历史上被奖励的总数
        只是计算中间变量，没有具体的业务含义的
        */ 
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
    }

    // 每一个LP池的信息
    struct PoolInfo {
        IERC20 lpToken;             // 代币地址，允许的代币
        uint256 allocPoint;         // 代币的分配比例，例如有三个池子abc对应的分数为a:b:c=10:20:10 那么在进行质押计算时,a=25%,b=50%,c=25%
        uint256 lastRewardTimestamp;    // 上次奖励发生时期
        uint256 accERC20PerShare;   // 累积单位token质押奖励数， *1e36（为了避免小数计算导致精度降低）.
        uint256 totalDeposits; // 总质押量
    }

    /* 下面是 farm 的基础属性*/
    // Farm奖励代币地址
    IERC20 public erc20;
    // 奖励代币已经发出去的数量
    uint256 public paidOut;
    // 奖励代币每秒奖励数
    uint256 public rewardPerSecond;
    // 总奖励数量（最多可奖励的数量）
    uint256 public totalRewards;
    // LP池信息
    PoolInfo[] public poolInfo;
    // 每个LP池用户的质押以及奖励信息 [PoolId,[用户地址，用户信息]]
    mapping(uint256 => mapping(address => UserInfo)) public userInfo;
    // 总分配分数
    uint256 public totalAllocPoint;

    // Farming 开始时间
    uint256 public startTimestamp;
    // Farming 结束时间
    uint256 public endTimestamp;

    //业务事件
    event Deposit(address indexed user, uint256 indexed pid, uint256 amount);
    event Withdraw(address indexed user, uint256 indexed pid, uint256 amount);
    event EmergencyWithdraw(address indexed user, uint256 indexed pid, uint256 amount);

    //初始化代币，每秒奖励数，开始时间
    // 调用父合约的Ownable 的构造方法设置owner
    constructor(IERC20 _erc20, uint256 _rewardPerSecond, uint256 _startTimestamp) 
        Ownable(msg.sender)   
    {
        erc20 = _erc20;
        rewardPerSecond = _rewardPerSecond;
        startTimestamp = _startTimestamp;
        endTimestamp = _startTimestamp;
    }

    // Number of LP pools
    function poolLength() external view returns (uint256) {
        return poolInfo.length;
    }

    // 平台方/ 项目方 向平台注入代币，代币注入之后会延长结束时间
    function fund(uint256 _amount) public {
        require(block.timestamp < endTimestamp, "fund: too late, the farm is closed");
        erc20.safeTransferFrom(address(msg.sender), address(this), _amount);
        endTimestamp += _amount/rewardPerSecond;
        totalRewards = totalRewards+_amount;
    }

    // 允许用户添加LP代币的一些信息
    // 只有当前合约的owner可以调用，添加完成之后LP流动池就初始化完成
    function add(uint256 _allocPoint, IERC20 _lpToken, bool _withUpdate) 
        public onlyOwner 
    {
        if (_withUpdate) {
            massUpdatePools();
        }
        uint256 lastRewardTimestamp = block.timestamp > startTimestamp ? block.timestamp : startTimestamp;
        totalAllocPoint = totalAllocPoint + _allocPoint;
        poolInfo.push(
            PoolInfo(
                {
                    lpToken : _lpToken,
                    allocPoint : _allocPoint,
                    lastRewardTimestamp : lastRewardTimestamp,
                    accERC20PerShare : 0,
                    totalDeposits : 0
                }
            )
        );
    }

    // 设置每个LP代币池的权重
    function set(uint256 _pid, uint256 _allocPoint, bool _withUpdate) public onlyOwner {
        if (_withUpdate) {
            massUpdatePools();
        }
        totalAllocPoint = totalAllocPoint-poolInfo[_pid].allocPoint+_allocPoint;
        poolInfo[_pid].allocPoint = _allocPoint;
    }

    // View function to see deposited LP for a user.
    function deposited(uint256 _pid, address _user) external view returns (uint256) {
        UserInfo storage user = userInfo[_pid][_user];
        return user.amount;
    }

    // 查看用户还可以获取的奖励
    function pending(uint256 _pid, address _user) external view returns (uint256) {
        PoolInfo storage pool = poolInfo[_pid];
        UserInfo storage user = userInfo[_pid][_user];
        uint256 accERC20PerShare = pool.accERC20PerShare;

        uint256 lpSupply = pool.totalDeposits;

        if (block.timestamp > pool.lastRewardTimestamp && lpSupply != 0) {
            uint256 lastTimestamp = block.timestamp < endTimestamp ? block.timestamp : endTimestamp;
            uint256 timestampToCompare = pool.lastRewardTimestamp < endTimestamp ? pool.lastRewardTimestamp : endTimestamp;
            // ppt 里面讲到的 上一次记录的时间到现在，经过的时间
            uint256 nrOfSeconds = lastTimestamp - timestampToCompare;
            uint256 erc20Reward = nrOfSeconds * rewardPerSecond * pool.allocPoint / totalAllocPoint;
            accERC20PerShare = accERC20PerShare + (erc20Reward * 1e36 / lpSupply);
        }
        return user.amount * accERC20PerShare / 1e36-user.rewardDebt;
    }

    // View function for total reward the farm has yet to pay out.
    function totalPending() external view returns (uint256) {
        if (block.timestamp <= startTimestamp) {
            return 0;
        }

        uint256 lastTimestamp = block.timestamp < endTimestamp ? block.timestamp : endTimestamp;
        // 现在到开始时间，已经发放的奖励
        return rewardPerSecond* (lastTimestamp - startTimestamp)- paidOut;
    }

    // Update reward variables for all pools. Be careful of gas spending!
    // 更新所有池子的奖励变量，小心gas消耗
    function massUpdatePools() public {
        uint256 length = poolInfo.length;
        for (uint256 pid = 0; pid < length; ++pid) {
            updatePool(pid);
        }
    }

    // Update reward variables of the given pool to be up-to-date.
    // 重要的函数
    /*
    在LP发生变化的时候，需要计算的中间变量
    */ 
    function updatePool(uint256 _pid) public {
        PoolInfo storage pool = poolInfo[_pid];
        uint256 lastTimestamp = block.timestamp < endTimestamp ? block.timestamp : endTimestamp;

        if (lastTimestamp <= pool.lastRewardTimestamp) {
            return;
        }
        uint256 lpSupply = pool.totalDeposits;

        if (lpSupply == 0) {
            pool.lastRewardTimestamp = lastTimestamp;
            return;
        }
        // 上一次更新时间到最新的奖励时间
        uint256 nrOfSeconds = lastTimestamp - pool.lastRewardTimestamp;
        // 这段时间要分配的代币数 = 从上一次奖励到现在，经历的时间 * 每秒分配的代币数 * 当前池子的权重
        uint256 erc20Reward = nrOfSeconds * rewardPerSecond * pool.allocPoint / totalAllocPoint;
        // 单位token累计奖励代币数 = 当前池子累计的奖励数量 + 新增时间分配代币数/总的LP数，
        pool.accERC20PerShare = pool.accERC20PerShare + erc20Reward * 1e36 / lpSupply;
        // 记录当前流动池奖励被更新的时间
        pool.lastRewardTimestamp = block.timestamp;
    }

    // Deposit LP tokens to Farm for ERC20 allocation.
    // 质押自己代币的方法
    function deposit(uint256 _pid, uint256 _amount) public {
        PoolInfo storage pool = poolInfo[_pid];
        UserInfo storage user = userInfo[_pid][msg.sender];

        // 充值的时候也会更新代币池的信息
        updatePool(_pid);

        // 充值的时候历史上有余额了，会把带分发的奖励分给用户
        if (user.amount > 0) {
            // 用户余额* 单位token分配的奖励数量 - 历史已经分配的奖励数量 = 用户待分发的奖励数量
            uint256 pendingAmount = user.amount * pool.accERC20PerShare / 1e36 - user.rewardDebt;
            erc20Transfer(msg.sender, pendingAmount);
        }
        // safe方法是要检查对方的地址是不是智能合约，并且会调用接收方的onERC20Received 回调方法确认，这个合约能处理代币。
        // 也就是说，1检查是否是合约，2 检查合约是否能处理代币
        pool.lpToken.safeTransferFrom(address(msg.sender), address(this), _amount);
        pool.totalDeposits = pool.totalDeposits + _amount;

        user.amount = user.amount + _amount;
        user.rewardDebt = user.amount * pool.accERC20PerShare / 1e36;
        emit Deposit(msg.sender, _pid, _amount);
    }

    // Withdraw LP tokens from Farm.
    // 包含两个功能，收取奖励，撤回质押
    function withdraw(uint256 _pid, uint256 _amount) public {
        PoolInfo storage pool = poolInfo[_pid];
        UserInfo storage user = userInfo[_pid][msg.sender];
        require(user.amount >= _amount, "withdraw: can't withdraw more than deposit");
        updatePool(_pid);

        // 计算奖励
        uint256 pendingAmount = user.amount*pool.accERC20PerShare/1e36-user.rewardDebt;

        erc20Transfer(msg.sender, pendingAmount);
        user.amount = user.amount-_amount;
        user.rewardDebt = user.amount*pool.accERC20PerShare/1e36;
        // 撤回流动性
        pool.lpToken.safeTransfer(address(msg.sender), _amount);
        pool.totalDeposits = pool.totalDeposits-_amount;

        emit Withdraw(msg.sender, _pid, _amount);
    }

    // Withdraw without caring about rewards. EMERGENCY ONLY.
    function emergencyWithdraw(uint256 _pid) public {
        PoolInfo storage pool = poolInfo[_pid];
        UserInfo storage user = userInfo[_pid][msg.sender];
        pool.lpToken.safeTransfer(address(msg.sender), user.amount);
        pool.totalDeposits = pool.totalDeposits-user.amount;
        emit EmergencyWithdraw(msg.sender, _pid, user.amount);
        user.amount = 0;
        user.rewardDebt = 0;
    }

    // Transfer ERC20 and update the required ERC20 to payout all rewards
    function erc20Transfer(address _to, uint256 _amount) internal {
        erc20.transfer(_to, _amount);
        paidOut += _amount;
    }
}
