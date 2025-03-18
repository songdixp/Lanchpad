//SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.0;

import "../interfaces/IAdmin.sol";
import "./C2NSale.sol";

/**
工厂合约
通过deploySale方法来构建sales合约，传入管理员地址，和分配质押的协议，设置合约是否是通过工厂生成的属性
可以判断合约类型
*/ 
contract SalesFactory {

    IAdmin public admin;
    address public allocationStaking;

    mapping (address => bool) public isSaleCreatedThroughFactory;

    mapping(address => address) public saleOwnerToSale;
    mapping(address => address) public tokenToSale;

    // Expose so query can be possible only by position as well
    address [] public allSales;

    event SaleDeployed(address saleContract);
    event SaleOwnerAndTokenSetInFactory(address sale, address saleOwner, address saleToken);

    modifier onlyAdmin {
        require(admin.isAdmin(msg.sender), "Only Admin can deploy sales");
        _;
    }

    // 部署默认的allocationStaking合约地址是 ZERO_ADDRESS 0x00... 地址
    constructor (address _adminContract, address _allocationStaking)  {
        admin = IAdmin(_adminContract);
        allocationStaking = _allocationStaking;
    }

    // Set allocation staking contract address.
    function setAllocationStaking(address _allocationStaking) public onlyAdmin {
        require(_allocationStaking != address(0));
        allocationStaking = _allocationStaking;
    }


    function deploySale()
        external
        onlyAdmin
    {   
        // 传入管理员地址，加上分配质押的合约地址，得到一个销售方案的合约
        // 使用合约实例化，是因为不知道 C2N的合约地址是多少，得先进行部署到链上
        C2NSale sale = new C2NSale(address(admin), allocationStaking);
        // 设置 sales 合约是否是通过工厂来进行创建的，必要的时候判断合约类型
        isSaleCreatedThroughFactory[address(sale)] = true;
        allSales.push(address(sale));

        emit SaleDeployed(address(sale));
    }

    /**
    下面的get 方法是获取部署成功的 sales 合约
    */ 
    // Function to return number of pools deployed
    function getNumberOfSalesDeployed() 
        external view 
        returns (uint) 
    {
        return allSales.length;
    }

    // Function
    function getLastDeployedSale() external view returns (address) {
        //
        if(allSales.length > 0) {
            return allSales[allSales.length - 1];
        }
        return address(0);
    }


    // Function to get all sales
    function getAllSales(uint startIndex, uint endIndex) external view returns (address[] memory) {
        require(endIndex > startIndex, "Bad input");

        address[] memory sales = new address[](endIndex - startIndex);
        uint index = 0;

        for(uint i = startIndex; i < endIndex; i++) {
            sales[index] = allSales[i];
            index++;
        }

        return sales;
    }

}
