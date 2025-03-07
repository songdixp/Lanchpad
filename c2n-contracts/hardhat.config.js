require("dotenv").config();
require("@openzeppelin/hardhat-upgrades");
require("@nomicfoundation/hardhat-toolbox");

/** @type import('hardhat/config').HardhatUserConfig */
module.exports = {
  networks: {
    hardhat: {
      chainId: 31337,
    },
    local: {
      url: "http://127.0.0.1:8545",
      chainId: 31337,
      accounts: [process.env.PRIVATE_KEY],
    },
    // sepolia: {
    //   url: "https://ethereum-sepolia.blockpi.network/v1/rpc/public",
    //   accounts: [process.env.PRIVATE_KEY],
    // },
  },
  solidity: {
    version: "0.8.26",
    settings: {
      optimizer: {
        enabled: true,
        runs: 200,
      },
      viaIR: true,
    },
  },
};
