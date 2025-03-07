const hre = require("hardhat");
const { saveContractAddress } = require("../utils");

/**
 * 此脚本用于部署售卖的代币（注意，跟质押奖励的TOKEN时不同的）
 */
async function main() {
  const tokenName = "MOCK-TOKEN";
  const symbol = "MCK";
  const totalSupply = "1000000000000000000000000000";
  const decimals = 18;

  const MCK = await hre.ethers.getContractFactory("C2NToken");
  const token = await MCK.deploy(tokenName, symbol, totalSupply, decimals);
  await token.waitForDeployment();
  console.log("MCK deployed to: ", await token.getAddress());

  saveContractAddress(hre.network.name, "MOCK-TOKEN", await token.getAddress());
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });
