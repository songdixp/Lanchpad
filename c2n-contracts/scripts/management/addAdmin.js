const hre = require("hardhat");
const { getSavedContractAddresses } = require("../utils");
const { ethers, web3 } = hre;

async function main() {
  const contracts = getSavedContractAddresses()[hre.network.name];
  const admin = await hre.ethers.getContractAt("Admin", contracts["Admin"]);
  await admin.addAdmin("0x2546BcD3c84621e976D8185a91A922aE77ECEc30");
  console.log(await admin.getAllAdmins());
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });
