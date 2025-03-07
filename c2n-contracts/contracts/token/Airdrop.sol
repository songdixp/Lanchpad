//SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.0;

import "@openzeppelin/contracts/token/ERC20/IERC20.sol";



contract Airdrop {


    IERC20 public airdropToken;
    uint256 public totalTokensWithdrawn;

    mapping (address => bool) public wasClaimed;
    uint256 public constant TOKENS_PER_CLAIM = 100 * 10**18;

    event TokensAirdropped(address beneficiary);

    // Constructor, initial setup
    constructor(address _airdropToken) {
        require(_airdropToken != address(0));

        airdropToken = IERC20(_airdropToken);
    }

    // Function to withdraw tokens.
    function withdrawTokens() public {
        require(msg.sender == tx.origin, "Require that message sender is tx-origin.");
        // require(!Address.isContract(msg.sender), "Contracts not allowed");  ds 推荐的判断方法，更安全


        address beneficiary = msg.sender;
        // 用户开始为false 取反 true，那么就不报错
        require(!wasClaimed[beneficiary], "Already claimed!");
        wasClaimed[msg.sender] = true;

        bool status = airdropToken.transfer(beneficiary, TOKENS_PER_CLAIM);
        require(status, "Token transfer status is false.");

        totalTokensWithdrawn = totalTokensWithdrawn+TOKENS_PER_CLAIM;
        emit TokensAirdropped(beneficiary);
    }

}
