const hre = require("hardhat");
const { saveContractAddress, getSavedContractAddresses } = require("../utils");

const salesConfig = require("../sales_config_refresher");
const yesno = require("yesno");
const { ethers } = hre;
/**
 * 此脚本是创建具体的销售流程，并将售卖的代币MCK设置到销售合约中
 */
async function main() {
  //开始刷新sales合约，如果是生产部署，这行需要删除，根据实际生产参数修改
  salesConfig.refreshSalesConfig(hre.network.name);
  const contracts = getSavedContractAddresses()[hre.network.name];
  const config = require("../configs/saleConfig.json");
  const c = config[hre.network.name];
  // getContractAt：这个方法用于获取已经部署在区块链上的合约实例。
  // 你需要提供合约的名字（或ABI）以及该合约的部署地址。
  // 这适用于当你知道合约已经在链上存在并且你想要与之交互的情况。
  const salesFactory = await hre.ethers.getContractAt(
    "SalesFactory",
    contracts["SalesFactory"]
  );

  let tx = await salesFactory.deploySale();
  await tx.wait();
  console.log("Sale is deployed successfully.");

  // let ok = await yesno({
  //     question: 'Are you sure you want to continue?'
  // });
  // if (!ok) {
  //     process.exit(0)
  // }

  const lastDeployedSale = await salesFactory.getLastDeployedSale();
  console.log("Deployed Sale address is: ", lastDeployedSale);

  const sale = await hre.ethers.getContractAt("C2NSale", lastDeployedSale);
  console.log(
    `Successfully instantiated sale contract at address: ${lastDeployedSale}.`
  );

  const totalTokens = ethers.parseEther(c["totalTokens"]);
  console.log("Total tokens to sell: ", c["totalTokens"]);

  const tokenPriceInEth = ethers.parseEther(c["tokenPriceInEth"]);
  console.log("tokenPriceInEth:", c["tokenPriceInEth"]);

  const saleOwner = c["saleOwner"];
  console.log("Sale owner is: ", c["saleOwner"]);

  const registrationStart = c["registrationStartAt"];
  const registrationEnd = registrationStart + c["registrationLength"];
  const saleStartTime = registrationEnd + c["delayBetweenRegistrationAndSale"];
  const saleEndTime = saleStartTime + c["saleRoundLength"];
  const maxParticipation = ethers.parseEther(c["maxParticipation"]);

  const tokensUnlockTime = c["TGE"];

  console.log("ready to set sale params");
  // ok = await yesno({
  //     question: 'Are you sure you want to continue?'
  // });
  // if (!ok) {
  //     process.exit(0)
  // }

  tx = await sale.setSaleParams(
    contracts["MOCK-TOKEN"],
    saleOwner,
    tokenPriceInEth,
    totalTokens,
    saleEndTime,
    tokensUnlockTime,
    c["portionVestingPrecision"],
    maxParticipation
  );
  await tx.wait();

  console.log("Sale Params set successfully.");

  console.log("Setting registration time.");

  // ok = await yesno({
  //     question: 'Are you sure you want to continue?'
  // });
  // if (!ok) {
  //     process.exit(0)
  // }
  //
  console.log("registrationStart:", registrationStart);
  console.log("registrationEnd:", registrationEnd);
  tx = await sale.setRegistrationTime(registrationStart, registrationEnd);
  await tx.wait();

  console.log("Registration time set.");

  console.log("Setting saleStart.");

  // ok = await yesno({
  //     question: 'Are you sure you want to continue?'
  // });
  // if (!ok) {
  //     process.exit(0)
  // }
  tx = await sale.setSaleStart(saleStartTime);
  await tx.wait();

  const unlockingTimes = c["unlockingTimes"];
  const percents = c["portionPercents"];

  console.log("Unlocking times: ", unlockingTimes);
  console.log("Percents: ", percents);
  console.log("Precision for vesting: ", c["portionVestingPrecision"]);
  console.log("Max vesting time shift in seconds: ", c["maxVestingTimeShift"]);

  console.log("Setting vesting params.");
  //
  // ok = await yesno({
  //     question: 'Are you sure you want to continue?'
  // });
  // if (!ok) {
  //     process.exit(0)
  // }
  tx = await sale.setVestingParams(
    unlockingTimes,
    percents,
    c["maxVestingTimeShift"]
  );
  await tx.wait();

  console.log("Vesting parameters set successfully.");

  console.log({
    saleAddress: lastDeployedSale,
    saleToken: contracts["MOCK-TOKEN"],
    saleOwner,
    tokenPriceInEth: tokenPriceInEth.toString(),
    totalTokens: totalTokens.toString(),
    saleEndTime,
    tokensUnlockTime,
    registrationStart,
    registrationEnd,
    saleStartTime,
  });

  console.log(
    JSON.stringify({
      saleAddress: lastDeployedSale,
      saleToken: contracts["MOCK-TOKEN"],
      saleOwner,
      tokenPriceInEth: tokenPriceInEth.toString(),
      totalTokens: totalTokens.toString(),
      saleEndTime,
      tokensUnlockTime,
      registrationStart,
      registrationEnd,
      saleStartTime,
    })
  );
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });
