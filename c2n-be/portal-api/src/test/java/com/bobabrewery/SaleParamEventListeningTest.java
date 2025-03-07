package com.bobabrewery;

import com.bobabrewery.repo.common.model.ProductPO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.assertj.core.util.Lists;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SaleParamEventListeningTest {

    /**
     * topic0,keccak256 of SaleCreated(address,uint256,uint256,uint256)
     * param0:saleOwner
     * param1:tokenPriceInETH
     * param2:amountOfTokensToSell
     * param3:saleEnd
     */



    private final static String CONTRACT_SALE_ADDRESS="0x8acd85898458400f7db866d53fcff6f0d49741ff";


    private static String SALE_EVENT_CREATED = "SaleCreated(address,uint256,uint256,uint256)";
    private final static String SET_SALES_PARAMS_EVENT="SaleCreated(address,uint256,uint256,uint256)";
    private static String SALE_EVENT_STARTTIME = "StartTimeSet(uint256)";
    private static String SALE_EVENT_REGISTRATIONTIME = "RegistrationTimeSet(uint256,uint256)";


    public static void  main(String[] args) throws Exception {

        Web3j web3 = Web3j.build(new HttpService("http://127.0.0.1:8545/"));  // defaults to http://localhost:8545/

        EthFilter filter = new EthFilter(DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST,Lists.newArrayList(CONTRACT_SALE_ADDRESS));

        web3.ethLogFlowable(filter).subscribe(eventLog -> {

            System.out.println("监听到事件变更,event="+eventLog.getTopics().get(0));

        });

        EthLog log= web3.ethGetLogs(filter).send();

        System.out.println(log);

    }
}
