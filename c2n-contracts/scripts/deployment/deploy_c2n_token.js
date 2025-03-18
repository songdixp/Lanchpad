const hre = require("hardhat");
const { saveContractAddress } = require("../utils");

/**
 * 此脚本用于部署C2N代币业务
 */
async function main() {
  const tokenName = "C2N";
  const symbol = "C2N";
  // const totalSupply = "1000000000000000000000000000";
  const totalSupply =  (1_000_000_000n * 10n**18n).toString(); 

  const decimals = 18;
  /**
   * getContractFactory：这个方法通常用于准备部署新的合约实例。
   * 它会编译<给定名称>的合约，并准备好一个工厂对象(C2N)，你可以用它来调用deploy方法并部署新的合约到区块链上。
   * */ 
  const C2N = await hre.ethers.getContractFactory("C2NToken");
  //构造时初始化一定数量的C2N代币
  const token = await C2N.deploy(tokenName, symbol, totalSupply, decimals);
  await token.waitForDeployment();
  console.log("C2N deployed to: ", await token.getAddress());

  saveContractAddress(hre.network.name, "C2N-TOKEN", await token.getAddress());
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });
