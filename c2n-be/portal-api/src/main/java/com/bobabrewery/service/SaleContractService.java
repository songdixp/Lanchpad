package com.bobabrewery.service;

import org.web3j.protocol.core.methods.response.Log;

import com.bobabrewery.repo.common.model.ProductPO;

/**
 * 销售合约服务
 */
public interface SaleContractService {

    /**
     * 根据销售合约地址查询最新的销售信息
     * @return
     */
    public ProductPO querySaleInfo(String  saleAddress) throws Exception;


    /**
     * 监听销售方案变更
     */
    public void listenSaleChange(String saleAddress,SaleChangeExecutor executor);


    /**
     * 销售变更执行器
     */
    interface  SaleChangeExecutor{
        /**
         * 执行逻辑
         * @param event
         */
        void execute(Log event);
    }
}
