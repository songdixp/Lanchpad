//SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.0;

import "@openzeppelin/contracts/token/ERC20/ERC20.sol";
 

contract C2NToken is ERC20 {

    uint8 private _decimals;

    constructor (string memory name_, string memory symbol_, uint256 totalSupply_, uint8 decimals_) ERC20( name_, symbol_) {
        _decimals=decimals_;
        _mint(_msgSender(), totalSupply_);
    }

    function decimals() public view override returns (uint8){
        return _decimals;
    }
    
    function burn(uint amount) external {
        _burn(_msgSender(), amount);
    }

    function mint(uint amount) external {
        _mint(_msgSender(), amount);
    }
}
