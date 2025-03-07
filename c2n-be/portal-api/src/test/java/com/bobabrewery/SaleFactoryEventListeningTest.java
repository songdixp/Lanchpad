package com.bobabrewery;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.assertj.core.util.Lists;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;

import com.bobabrewery.repo.common.model.ProductPO;

public class SaleFactoryEventListeningTest {

    /**
     * topic0,keccak256 of SaleCreated(address,uint256,uint256,uint256)
     * param0:saleOwner
     * param1:tokenPriceInETH
     * param2:amountOfTokensToSell
     * param3:saleEnd
     */


    private final static String SET_SALES_FACTORY_EVENT="SaleDeployed(address)";
    private final static String CONTRACT_SALE_ADDRESS="0x8A791620dd6260079BF849Dc5567aDC3F2FdC318";


    public static void main(String[] args) throws Exception {
        Web3j web3 = Web3j.build(new HttpService("http://127.0.0.1:8545/"));  // defaults to http://localhost:8545/
        Web3ClientVersion web3ClientVersion = web3.web3ClientVersion().send();

        EthFilter filter = new EthFilter(DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST,Lists.newArrayList(CONTRACT_SALE_ADDRESS));
        filter.addOptionalTopics(EventEncoder.buildEventSignature(SET_SALES_FACTORY_EVENT));



        web3.ethLogFlowable(filter).subscribe(eventLog -> {
            if (eventLog.getAddress() !=""
                    && CollectionUtils.isNotEmpty(eventLog.getTopics())
                    && eventLog.getTopics().get(0).toLowerCase().equals(EventEncoder.buildEventSignature(SET_SALES_FACTORY_EVENT))) {
                //decode data to (address,uint256,uint256,uint256)

               String address=FunctionReturnDecoder.decodeAddress(eventLog.getData());

                //常量
                System.out.println(address);

            }
        });
    }



}
