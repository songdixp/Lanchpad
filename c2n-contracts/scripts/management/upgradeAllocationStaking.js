// scripts/upgrade-box.js
const hre = require("hardhat");
const {
  getSavedContractAddresses,
  getSavedProxyABI,
  saveContractAddress,
} = require("../utils");

async function main() {
  const contracts = getSavedContractAddresses()[hre.network.name];
  const proxyAdminAbi = getSavedProxyABI()["ProxyAdmin"];

  // console.log(proxyAdminAbi);
  const proxyAdmin = await hre.ethers.getContractAt(
    proxyAdminAbi,
    contracts["ProxyAdmin"]
  );

  const allocationStakingProxy = contracts["AllocationStakingProxy"];
  console.log("Proxy:", allocationStakingProxy);

  const AllocationStakingImplementation = await ethers.getContractFactory(
    "AllocationStaking"
  );
  const allocationStakingImpl = await AllocationStakingImplementation.deploy();

  console.log("New Implementation:", await allocationStakingImpl.getAddress());

  await hre.upgrades.upgradeProxy(
    allocationStakingProxy,
    AllocationStakingImplementation
  );
  console.log("AllocationStaking contract upgraded");
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });
