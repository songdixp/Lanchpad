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
  const c = config[hre.network.name];
  const contracts = getSavedContractAddresses()[hre.network.name];

  const ZERO_ADDRESS = "0x0000000000000000000000000000000000000000";

  const Admin = await ethers.getContractFactory("Admin");
  console.log("ready to deploy admin");

  //部署管理员合约并设置管理员
  const admin = await Admin.deploy(c.admins);
  await admin.waitForDeployment();
  console.log("Admin contract deployed to: ", await admin.getAddress());
  saveContractAddress(hre.network.name, "Admin", await admin.getAddress());

  //部署销售工厂
  console.log("ready to deploy salesFactory ");
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

  //通过透明升级合约模式部署分配质押
  console.log("ready to deploy AllocationStaking ");
  const currentTimestamp = await getCurrentBlockTimestamp();
  console.log("Farming starts at: ", currentTimestamp);
  const AllocationStaking = await ethers.getContractFactory(
    "AllocationStaking"
  );
  const allocationStaking = await upgrades.deployProxy(
    AllocationStaking,
    [
      contracts["C2N-TOKEN"],
      ethers.parseEther(c.allocationStakingRPS),
      currentTimestamp + c.delayBeforeStart,
      await salesFactory.getAddress(),
    ],
    { unsafeAllow: ["delegatecall"] }
  );
  await allocationStaking.waitForDeployment();
  console.log(
    "AllocationStaking Proxy deployed to:",
    await allocationStaking.getAddress()
  );
  saveContractAddress(
    hre.network.name,
    "AllocationStakingProxy",
    await allocationStaking.getAddress()
  );

  let proxyAdminContract = await upgrades.erc1967.getAdminAddress(
    await allocationStaking.getAddress()
  );
  saveContractAddress(hre.network.name, "ProxyAdmin", proxyAdminContract);
  console.log("Proxy Admin address is : ", proxyAdminContract);

  console.log("ready to setAllocationStaking params ");
  await salesFactory.setAllocationStaking(await allocationStaking.getAddress());
  console.log(
    `salesFactory.setAllocationStaking ${await allocationStaking.getAddress()} done.;`
  );

  const totalRewards = ethers.parseEther(c.initialRewardsAllocationStaking);

  const token = await hre.ethers.getContractAt(
    "C2NToken",
    contracts["C2N-TOKEN"]
  );

  //将总奖励的代币数授权给allocationStaking
  console.log(
    "ready to approve ",
    c.initialRewardsAllocationStaking,
    " token to staking  "
  );

  let tx = await token.approve(
    await allocationStaking.getAddress(),
    totalRewards
  );
  await tx.wait();
  console.log(
    `token.approve(${await allocationStaking.getAddress()}, ${totalRewards.toString()});`
  );

  console.log("ready to add c2n to pool");
  // add c2n to pool
  tx = await allocationStaking.add(100, await token.getAddress(), true);
  await tx.wait();
  console.log(`allocationStaking.add(${await token.getAddress()});`);

  // fund tokens for testing
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
