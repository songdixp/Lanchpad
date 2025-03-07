package com.bobabrewery.service.impl;

import java.util.Date;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.web3j.abi.EventEncoder;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.http.HttpService;
import org.web3j.tuples.generated.Tuple14;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tx.gas.DefaultGasProvider;

import com.bobabrewery.repo.common.model.ProductPO;
import com.bobabrewery.service.SaleContractService;
import com.bobabrewery.util.C2NSale;
import com.bobabrewery.util.CredentialsUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 销售合约服务，
 */
@Component
@Slf4j
public class SaleContractServiceImpl implements SaleContractService {

    @Resource
    private CredentialsUtils credentialsUtils;

    /**
     * 事件topic-销售合约创建
     */
    private static String SALE_EVENT_CREATED = "SaleCreated(address,uint256,uint256,uint256)";

    /**
     * 事件topic-销售合约修改创建时间
     */
    private static String SALE_EVENT_STARTTIME = "StartTimeSet(uint256)";

    /**
     * 修改注册时间
     */
    private static String SALE_EVENT_REGISTRATIONTIME = "RegistrationTimeSet(uint256,uint256)";

    /**
     * @see SaleContractService#querySaleInfo(String)
     */
    @Override
    public ProductPO querySaleInfo(String saleAddress) throws Exception {
        Web3j web3 = Web3j.build(new HttpService(credentialsUtils.networkUrl)); // defaults to http://localhost:8545/

        DefaultGasProvider contractGasProvider = new DefaultGasProvider();
        Credentials credentials = Credentials.create(credentialsUtils.privateKey);
        C2NSale contractInstance = C2NSale.load(saleAddress, web3, credentials, contractGasProvider);
        Tuple14 saleInfo = contractInstance.sale().send();
        Tuple3 registrationInfo = contractInstance.registration().send();
        // 从contract.sale.token获取
        String _saleToken = saleInfo.component1().toString();
        // event.saleOwner
        String _saleOwner = saleInfo.component6().toString();
        // event.tokenPriceInETH
        String tokenPriceInEth = saleInfo.component7().toString();
        // event.amountOfTokenToSell
        String totalTokens = saleInfo.component8().toString();
        // event.saleEnd
        String saleEndTime = saleInfo.component12().toString();

        // contract.sale.tokensUnlockTime
        String tokensUnlockTime = saleInfo.component13().toString();
        // contract.registration.registrationTimeStarts
        String registrationStart = registrationInfo.component1().toString();
        // contract.registration.registrationTimeEnds
        String registrationEnd = registrationInfo.component2().toString();
        // contract.sale.saleStart
        String saleStartTime = saleInfo.component11().toString();

        ProductPO productPO = new ProductPO();
        productPO.setId(3);
        productPO.setSaleAddress(contractInstance.getContractAddress());
        productPO.setSaleToken(_saleToken);
        productPO.setSaleOwner(_saleOwner);
        productPO.setRegistrationEnd(new Date(Long.parseLong(registrationEnd.concat("000"))));
        productPO.setRegistrationStart(new Date(Long.parseLong(registrationStart.concat("000"))));
        productPO.setSaleEndTime(new Date(Long.parseLong(saleEndTime.concat("000"))));
        productPO.setTotalTokens(totalTokens);
        productPO.setTokensUnlockTime(new Date(Long.parseLong(tokensUnlockTime.concat("000"))));
        productPO.setSaleStartTime(new Date(Long.parseLong(saleStartTime.concat("000"))));
        productPO.setTokenPriceInPT(tokenPriceInEth);
        return productPO;
    }

    /**
     * @see SaleContractService#listenSaleChange(String, SaleChangeExecutor)
     */
    @Override
    public void listenSaleChange(String saleAddress, SaleChangeExecutor executor) {
        Web3j web3 = Web3j.build(new HttpService(credentialsUtils.networkUrl)); // defaults to http://localhost:8545/

        EthFilter filter = new EthFilter(DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST, saleAddress);
        filter.addOptionalTopics(EventEncoder.buildEventSignature(SALE_EVENT_CREATED), EventEncoder.buildEventSignature(SALE_EVENT_STARTTIME),
            EventEncoder.buildEventSignature(SALE_EVENT_REGISTRATIONTIME));
        web3.ethLogFlowable(filter).subscribe(eventLog -> {
            log.info(String.format("监听到销售变更消息,address=%s,topic0=%s", saleAddress, eventLog.getTopics().get(0)));
            executor.execute(eventLog);

        });



    }


}
