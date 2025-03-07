const hre = require("hardhat");
const { saveContractAddress, getSavedContractAddresses } = require("../utils");
const { ethers } = require("hardhat");
/**
 * 此脚本用于部署farm业务
 */
async function main() {
  const RPS = "1";
  const now = Math.round(new Date().getTime() / 1000);
  //开始时间为当前时间之后的100s，实际应用时根据自己的业务诉求变更
  const startTS = now + 100;
  // get c2n token address from contract address file
  const c2nTokenAddress =
    getSavedContractAddresses()[hre.network.name]["C2N-TOKEN"];
  console.log("c2nTokenAddress: ", c2nTokenAddress);

  const farm = await hre.ethers.getContractFactory("FarmingC2N");
  const Farm = await farm.deploy(
    c2nTokenAddress,
    ethers.parseEther(RPS),
    startTS
  );
  await Farm.waitForDeployment();
  console.log("Farm deployed to: ", await Farm.getAddress());

  saveContractAddress(hre.network.name, "FarmingC2N", await Farm.getAddress());

  // fund the farm
  // approve the farm to spend the token
  const C2N = await hre.ethers.getContractAt("C2NToken", c2nTokenAddress);
  const approveTx = await C2N.approve(
    await Farm.getAddress(),
    ethers.parseEther("50000")
  );
  await approveTx.wait();
  let tx = await Farm.fund(ethers.parseEther("50000"));
  await tx.wait();
  // add lp token
  const lpTokenAddress =
    getSavedContractAddresses()[hre.network.name]["C2N-TOKEN"];
  await Farm.add(100, lpTokenAddress, true);
  console.log("Farm funded and LP token added");
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });
