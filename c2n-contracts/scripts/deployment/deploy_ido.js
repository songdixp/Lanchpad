const hre = require("hardhat");
const { ethers, upgrades } = require("hardhat");
const { saveContractAddress, getSavedContractAddresses } = require("../utils");
const config = require("../configs/saleConfig.json");
// const yesno = require('yesno');

async function getCurrentBlockTimestamp() {
  return (await ethers.provider.getBlock("latest")).timestamp;
}
/**
 * 此脚本用于部署ido业务相关合约，例如admin，销售工厂，分配质押
 */
async function main() {
  const c = config[hre.network.name]; // local的json数据块
  const contracts = getSavedContractAddresses()[hre.network.name];

  const ZERO_ADDRESS = "0x0000000000000000000000000000000000000000";

  // Admin.sol
  //部署管理员合约
  const Admin = await ethers.getContractFactory("Admin");
  console.log("ready to deploy admin");
  //部署管理员合约并设置管理员 c.admins admin的地址 目前是测试链上的第一个账户地址
  const admin = await Admin.deploy(c.admins);
  await admin.waitForDeployment();
  console.log("Admin contract deployed to: ", await admin.getAddress());
  /*
  将Admin 字符写到 contract-addresses.json 文件中 
  {
    "local": { local 就是网络名称
        "Admin": "0x2279B7A0a67DB372996a5FaB50D91eAA73d2eBe6", 写入 Admin 后面是地址
    }
   }
  */ 
  saveContractAddress(hre.network.name, "Admin", await admin.getAddress());

  // SalesFactory.sol
  //部署销售工厂合约
  console.log("ready to deploy SalesFactory ");
  const SalesFactory = await ethers.getContractFactory("SalesFactory");
  const salesFactory = await SalesFactory.deploy(
    await admin.getAddress(),
    ZERO_ADDRESS
  );
  await salesFactory.waitForDeployment();
  saveContractAddress(
    hre.network.name,
    "SalesFactory",
    await salesFactory.getAddress()
  );
  console.log("Sales factory deployed to: ", await salesFactory.getAddress());

  // AllocationStaking.sol
  //通过透明升级合约模式部署分配质押
  console.log("ready to deploy AllocationStaking ");
  const currentTimestamp = await getCurrentBlockTimestamp();
  console.log("Farming starts at: ", currentTimestamp);
  const AllocationStaking = await ethers.getContractFactory("AllocationStaking");
  const allocationStaking = await upgrades.deployProxy(
    AllocationStaking,
    [
      contracts["C2N-TOKEN"],
      ethers.parseEther(c.allocationStakingRPS), // 每秒奖励 0.1
      currentTimestamp + c.delayBeforeStart,
      await salesFactory.getAddress(),
    ],
    // 选项对象，用于配置透明代理合约的行为，有 initializer 、kind、 unsafeAllow、 call、 proxyAdmin、 gasLimit、 value
    { unsafeAllow: ["delegatecall"] }
  );
  await allocationStaking.waitForDeployment();
  console.log("allocationStaking Proxy deployed to:", await allocationStaking.getAddress());
  saveContractAddress(hre.network.name, "AllocationStakingProxy", await allocationStaking.getAddress());

  /**
   * ERC-1967 是以太坊上的一个标准，定义了如何在智能合约中存储和管理<代理合约>的相关信息
   * 关键信息如下：
   * 1、实施地址 implementation address 代理合约指向的实现合约的地址
   * 2、管理员地址 Admin Address 有权升级代理合约的地址
   * */ 
  let proxyAdminContract = await upgrades.erc1967.getAdminAddress(await allocationStaking.getAddress());
  saveContractAddress(hre.network.name, "ProxyAdmin", proxyAdminContract);
  console.log("Proxy Admin address is : ", proxyAdminContract);

  // 销售工厂合约，设置 AllocationStaking 合约的地址
  console.log("ready to setAllocationStaking params: ");
  await salesFactory.setAllocationStaking(await allocationStaking.getAddress());
  console.log(`salesFactory.setAllocationStaking ${await allocationStaking.getAddress()} done.;`);

  // 1000000 总共的奖励代币 
  const totalRewards = ethers.parseEther(c.initialRewardsAllocationStaking);
  // 获取已经在链上部署的合约地址
  const token = await hre.ethers.getContractAt("C2NToken", contracts["C2N-TOKEN"]);

  //将总奖励的代币数授权给allocationStaking
  console.log("ready to approve ", c.initialRewardsAllocationStaking, " token to staking ");
  // C2N-TOKEN 是 ERC20 标准，允许 allocationStaking 代理合约地址转账的额度
  let tx = await token.approve(
    await allocationStaking.getAddress(),
    totalRewards
  );
  await tx.wait();
  console.log(
    `token.approve(${await allocationStaking.getAddress()}, ${totalRewards.toString()});`
  );

  console.log("ready to add c2n to pool");
  // add c2n to pool 初始的代币 C2N  权重是100，因为只有这么一个代币，在代币池里面
  tx = await allocationStaking.add(100, await token.getAddress(), true);
  await tx.wait();
  console.log(`allocationStaking.add(${await token.getAddress()});`);

  // fund tokens for testing  1000000
  const fund = Math.floor(Number(c.initialRewardsAllocationStaking)).toString();
  console.log(`ready to fund ${fund} token for testing`);
  // Fund only 50000 tokens, for testing
  // sleep(5000)
  //将质押奖励代币（在这里时C2N）转移到allocationStaking
  await allocationStaking.fund(ethers.parseEther(fund));
  console.log("Funded tokens");
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });
