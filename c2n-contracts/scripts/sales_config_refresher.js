const fs = require("fs");
const path = require("path");

function getSalesConfig() {
  let json;
  try {
    json = fs.readFileSync(
      path.join(__dirname, `../scripts/configs/saleConfig.json`)
    );
  } catch (err) {
    console.log(err);
    json = "{}";
  }
  return JSON.parse(json);
}

function refreshSalesConfig(network) {
  const salesConfig = getSalesConfig();
  const now = Math.round(new Date().getTime() / 1000); // Unix timestamp in milliseconds
  //registrationStartAt  为timestamp+300s
  const registrationStartAt = now + 300;
  //TGE 为timestamp+720s
  const tge = now + 720;
  //UNLOCK 为 为timestamp+840s
  const unlock = now + 840;
  salesConfig[network]["registrationStartAt"] = registrationStartAt;
  salesConfig[network]["TGE"] = tge;
  salesConfig[network]["unlockingTimes"] = [unlock];
  fs.writeFileSync(
    path.join(__dirname, "../scripts/configs/saleConfig.json"),
    JSON.stringify(salesConfig, null, "    ")
  );
}

module.exports = {
  getSalesConfig,
  refreshSalesConfig,
};
