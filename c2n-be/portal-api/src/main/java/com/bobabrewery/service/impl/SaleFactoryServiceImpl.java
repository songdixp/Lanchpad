package com.bobabrewery.service.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.bobabrewery.service.IProjectService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.http.HttpService;

import com.bobabrewery.repo.common.model.ProductPO;
import com.bobabrewery.service.SaleContractService;
import com.bobabrewery.service.SaleFactoryService;
import com.bobabrewery.util.CredentialsUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 销售工厂服务
 */
@Component
@Slf4j
public class SaleFactoryServiceImpl implements SaleFactoryService {
    @Resource
    private CredentialsUtils credentialsUtils;

    @Resource
    private SaleContractService saleContractService;
    @Value("${contract.saleFactory.address}")
    private String saleFactoryAddress;


    private String SALE_DEPLOY_TOPIC = "SaleDeployed(address)";


    @Resource
    private IProjectService productContractService;

    /**
     * 启动销售工厂监听
     */
    @PostConstruct
    public void init() {

        log.info(String.format("提交开启销售工厂监听,监听销售创建,address=%s", saleFactoryAddress));
        startSaleFactoryListen();
    }

    @Override
    public void startSaleFactoryListen() {
        Web3j web3 = Web3j.build(new HttpService(credentialsUtils.networkUrl));
        EthFilter filter = new EthFilter(DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST, saleFactoryAddress);
        filter.addSingleTopic(EventEncoder.buildEventSignature(SALE_DEPLOY_TOPIC));
        web3.ethLogFlowable(filter).subscribe(eventLog -> {

            String _data = eventLog.getData();
            String saleAddress = FunctionReturnDecoder.decodeAddress(_data);
//            executorService.submit(new SaleParamChangeRunnable(saleAddress));
            saleContractService.listenSaleChange(saleAddress, event -> {
                try {
                    ProductPO productPO = saleContractService.querySaleInfo(saleAddress);
                    productContractService.updateByProduct(productPO);
                    log.info(String.format("监听到销售变更消息,组装productPo=%s", productPO));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });

    }

//    class SaleParamChangeRunnable implements Runnable {
//
//        /**
//         * 销售地址
//         */
//        private String saleAddress;
//
//        public SaleParamChangeRunnable(String _saleAddress) {
//            saleAddress = _saleAddress;
//        }
//
//        @Override
//        public void run() {
//            log.info(String.format("提交销售协议变更事件的监听,address=%s", saleAddress));
//            saleContractService.listenSaleChange(saleAddress, event -> {
//                try {
//                    ProductPO productPO = saleContractService.querySaleInfo(saleAddress);
//                    productContractService.updateByProduct(productPO);
//                    log.info(String.format("监听到销售变更消息,组装productPo=%s", productPO));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            });
//        }
//    }
}
