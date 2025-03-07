package com.bobabrewery;

import com.bobabrewery.repo.common.model.ProductPO;
import com.bobabrewery.util.C2NSale;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.tuples.generated.Tuple14;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tx.gas.DefaultGasProvider;

import java.util.Date;

public class TransactionCallTest {

    public static void main(String[] args) throws Exception {
        Web3j web3 = Web3j.build(new HttpService("http://127.0.0.1:8545/")); // defaults to http://localhost:8545/
        Web3ClientVersion web3ClientVersion = web3.web3ClientVersion().send();
        String clientVersion = web3ClientVersion.getWeb3ClientVersion();

        DefaultGasProvider contractGasProvider = new DefaultGasProvider();
        Credentials credentials = Credentials.create("0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80");
        C2NSale contractInstance = C2NSale.load("0x8acd85898458400f7db866d53fcff6f0d49741ff", web3, credentials, contractGasProvider);
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

        System.out.println(productPO);
    }
}
