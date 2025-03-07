package com.bobabrewery.util;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple14;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tuples.generated.Tuple4;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/hyperledger-web3j/web3j/tree/main/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 1.6.2.
 */
@SuppressWarnings("rawtypes")
public class C2NSale extends Contract {
    public static final String BINARY = "Bin file was not provided";

    public static final String FUNC_ADMIN = "admin";

    public static final String FUNC_ALLOCATIONSTAKINGCONTRACT = "allocationStakingContract";

    public static final String FUNC_CHECKPARTICIPATIONSIGNATURE = "checkParticipationSignature";

    public static final String FUNC_CHECKREGISTRATIONSIGNATURE = "checkRegistrationSignature";

    public static final String FUNC_DEPOSITTOKENS = "depositTokens";

    public static final String FUNC_EXTENDREGISTRATIONPERIOD = "extendRegistrationPeriod";

    public static final String FUNC_FACTORY = "factory";

    public static final String FUNC_GETNUMBEROFREGISTEREDUSERS = "getNumberOfRegisteredUsers";

    public static final String FUNC_GETPARTICIPATION = "getParticipation";

    public static final String FUNC_GETPARTICIPATIONSIGNER = "getParticipationSigner";

    public static final String FUNC_GETVESTINGINFO = "getVestingInfo";

    public static final String FUNC_ISPARTICIPATED = "isParticipated";

    public static final String FUNC_ISREGISTERED = "isRegistered";

    public static final String FUNC_MAXVESTINGTIMESHIFT = "maxVestingTimeShift";

    public static final String FUNC_NUMBEROFPARTICIPANTS = "numberOfParticipants";

    public static final String FUNC_PARTICIPATE = "participate";

    public static final String FUNC_PORTIONVESTINGPRECISION = "portionVestingPrecision";

    public static final String FUNC_POSTPONESALE = "postponeSale";

    public static final String FUNC_REGISTERFORSALE = "registerForSale";

    public static final String FUNC_REGISTRATION = "registration";

    public static final String FUNC_SALE = "sale";

    public static final String FUNC_SETCAP = "setCap";

    public static final String FUNC_SETREGISTRATIONTIME = "setRegistrationTime";

    public static final String FUNC_SETSALEPARAMS = "setSaleParams";

    public static final String FUNC_SETSALESTART = "setSaleStart";

    public static final String FUNC_SETSALETOKEN = "setSaleToken";

    public static final String FUNC_SETVESTINGPARAMS = "setVestingParams";

    public static final String FUNC_SHIFTVESTINGUNLOCKINGTIMES = "shiftVestingUnlockingTimes";

    public static final String FUNC_UPDATETOKENPRICEINETH = "updateTokenPriceInETH";

    public static final String FUNC_USERTOPARTICIPATION = "userToParticipation";

    public static final String FUNC_VESTINGPERCENTPERPORTION = "vestingPercentPerPortion";

    public static final String FUNC_VESTINGPORTIONSUNLOCKTIME = "vestingPortionsUnlockTime";

    public static final String FUNC_WITHDRAWEARNINGS = "withdrawEarnings";

    public static final String FUNC_WITHDRAWEARNINGSANDLEFTOVER = "withdrawEarningsAndLeftover";

    public static final String FUNC_WITHDRAWLEFTOVER = "withdrawLeftover";

    public static final String FUNC_WITHDRAWMULTIPLEPORTIONS = "withdrawMultiplePortions";

    public static final String FUNC_WITHDRAWTOKENS = "withdrawTokens";

    public static final Event MAXPARTICIPATIONSET_EVENT = new Event("MaxParticipationSet", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
    ;

    public static final Event REGISTRATIONTIMESET_EVENT = new Event("RegistrationTimeSet", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event SALECREATED_EVENT = new Event("SaleCreated", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event STARTTIMESET_EVENT = new Event("StartTimeSet", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
    ;

    public static final Event TOKENPRICESET_EVENT = new Event("TokenPriceSet", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
    ;

    public static final Event TOKENSSOLD_EVENT = new Event("TokensSold", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event TOKENSWITHDRAWN_EVENT = new Event("TokensWithdrawn", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event USERREGISTERED_EVENT = new Event("UserRegistered", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
    ;

    @Deprecated
    protected C2NSale(String contractAddress, Web3j web3j, Credentials credentials,
                      BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected C2NSale(String contractAddress, Web3j web3j, Credentials credentials,
                      ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected C2NSale(String contractAddress, Web3j web3j,
                      TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected C2NSale(String contractAddress, Web3j web3j,
                      TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<MaxParticipationSetEventResponse> getMaxParticipationSetEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(MAXPARTICIPATIONSET_EVENT, transactionReceipt);
        ArrayList<MaxParticipationSetEventResponse> responses = new ArrayList<MaxParticipationSetEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            MaxParticipationSetEventResponse typedResponse = new MaxParticipationSetEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.maxParticipation = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static MaxParticipationSetEventResponse getMaxParticipationSetEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(MAXPARTICIPATIONSET_EVENT, log);
        MaxParticipationSetEventResponse typedResponse = new MaxParticipationSetEventResponse();
        typedResponse.log = log;
        typedResponse.maxParticipation = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<MaxParticipationSetEventResponse> maxParticipationSetEventFlowable(
            EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getMaxParticipationSetEventFromLog(log));
    }

    public Flowable<MaxParticipationSetEventResponse> maxParticipationSetEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(MAXPARTICIPATIONSET_EVENT));
        return maxParticipationSetEventFlowable(filter);
    }

    public static List<RegistrationTimeSetEventResponse> getRegistrationTimeSetEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(REGISTRATIONTIMESET_EVENT, transactionReceipt);
        ArrayList<RegistrationTimeSetEventResponse> responses = new ArrayList<RegistrationTimeSetEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            RegistrationTimeSetEventResponse typedResponse = new RegistrationTimeSetEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.registrationTimeStarts = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.registrationTimeEnds = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static RegistrationTimeSetEventResponse getRegistrationTimeSetEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(REGISTRATIONTIMESET_EVENT, log);
        RegistrationTimeSetEventResponse typedResponse = new RegistrationTimeSetEventResponse();
        typedResponse.log = log;
        typedResponse.registrationTimeStarts = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.registrationTimeEnds = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<RegistrationTimeSetEventResponse> registrationTimeSetEventFlowable(
            EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getRegistrationTimeSetEventFromLog(log));
    }

    public Flowable<RegistrationTimeSetEventResponse> registrationTimeSetEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(REGISTRATIONTIMESET_EVENT));
        return registrationTimeSetEventFlowable(filter);
    }

    public static List<SaleCreatedEventResponse> getSaleCreatedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(SALECREATED_EVENT, transactionReceipt);
        ArrayList<SaleCreatedEventResponse> responses = new ArrayList<SaleCreatedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            SaleCreatedEventResponse typedResponse = new SaleCreatedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.saleOwner = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.tokenPriceInETH = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.amountOfTokensToSell = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            typedResponse.saleEnd = (BigInteger) eventValues.getNonIndexedValues().get(3).getValue();
            typedResponse.tokensUnlockTime = (BigInteger) eventValues.getNonIndexedValues().get(4).getValue();
            typedResponse.registrationStart = (BigInteger) eventValues.getNonIndexedValues().get(5).getValue();
            typedResponse.registrationEnd = (BigInteger) eventValues.getNonIndexedValues().get(6).getValue();
            typedResponse.saleStart = (BigInteger) eventValues.getNonIndexedValues().get(7).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static SaleCreatedEventResponse getSaleCreatedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(SALECREATED_EVENT, log);
        SaleCreatedEventResponse typedResponse = new SaleCreatedEventResponse();
        typedResponse.log = log;
        typedResponse.saleOwner = (String) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.tokenPriceInETH = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        typedResponse.amountOfTokensToSell = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
        typedResponse.saleEnd = (BigInteger) eventValues.getNonIndexedValues().get(3).getValue();
        typedResponse.tokensUnlockTime = (BigInteger) eventValues.getNonIndexedValues().get(4).getValue();
        typedResponse.registrationStart = (BigInteger) eventValues.getNonIndexedValues().get(5).getValue();
        typedResponse.registrationEnd = (BigInteger) eventValues.getNonIndexedValues().get(6).getValue();
        typedResponse.saleStart = (BigInteger) eventValues.getNonIndexedValues().get(7).getValue();
        return typedResponse;
    }

    public Flowable<SaleCreatedEventResponse> saleCreatedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getSaleCreatedEventFromLog(log));
    }

    public Flowable<SaleCreatedEventResponse> saleCreatedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(SALECREATED_EVENT));
        return saleCreatedEventFlowable(filter);
    }

    public static List<StartTimeSetEventResponse> getStartTimeSetEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(STARTTIMESET_EVENT, transactionReceipt);
        ArrayList<StartTimeSetEventResponse> responses = new ArrayList<StartTimeSetEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            StartTimeSetEventResponse typedResponse = new StartTimeSetEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.startTime = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static StartTimeSetEventResponse getStartTimeSetEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(STARTTIMESET_EVENT, log);
        StartTimeSetEventResponse typedResponse = new StartTimeSetEventResponse();
        typedResponse.log = log;
        typedResponse.startTime = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<StartTimeSetEventResponse> startTimeSetEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getStartTimeSetEventFromLog(log));
    }

    public Flowable<StartTimeSetEventResponse> startTimeSetEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(STARTTIMESET_EVENT));
        return startTimeSetEventFlowable(filter);
    }

    public static List<TokenPriceSetEventResponse> getTokenPriceSetEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(TOKENPRICESET_EVENT, transactionReceipt);
        ArrayList<TokenPriceSetEventResponse> responses = new ArrayList<TokenPriceSetEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            TokenPriceSetEventResponse typedResponse = new TokenPriceSetEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.newPrice = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static TokenPriceSetEventResponse getTokenPriceSetEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(TOKENPRICESET_EVENT, log);
        TokenPriceSetEventResponse typedResponse = new TokenPriceSetEventResponse();
        typedResponse.log = log;
        typedResponse.newPrice = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<TokenPriceSetEventResponse> tokenPriceSetEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getTokenPriceSetEventFromLog(log));
    }

    public Flowable<TokenPriceSetEventResponse> tokenPriceSetEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(TOKENPRICESET_EVENT));
        return tokenPriceSetEventFlowable(filter);
    }

    public static List<TokensSoldEventResponse> getTokensSoldEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(TOKENSSOLD_EVENT, transactionReceipt);
        ArrayList<TokensSoldEventResponse> responses = new ArrayList<TokensSoldEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            TokensSoldEventResponse typedResponse = new TokensSoldEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.user = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static TokensSoldEventResponse getTokensSoldEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(TOKENSSOLD_EVENT, log);
        TokensSoldEventResponse typedResponse = new TokensSoldEventResponse();
        typedResponse.log = log;
        typedResponse.user = (String) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<TokensSoldEventResponse> tokensSoldEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getTokensSoldEventFromLog(log));
    }

    public Flowable<TokensSoldEventResponse> tokensSoldEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(TOKENSSOLD_EVENT));
        return tokensSoldEventFlowable(filter);
    }

    public static List<TokensWithdrawnEventResponse> getTokensWithdrawnEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(TOKENSWITHDRAWN_EVENT, transactionReceipt);
        ArrayList<TokensWithdrawnEventResponse> responses = new ArrayList<TokensWithdrawnEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            TokensWithdrawnEventResponse typedResponse = new TokensWithdrawnEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.user = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static TokensWithdrawnEventResponse getTokensWithdrawnEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(TOKENSWITHDRAWN_EVENT, log);
        TokensWithdrawnEventResponse typedResponse = new TokensWithdrawnEventResponse();
        typedResponse.log = log;
        typedResponse.user = (String) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<TokensWithdrawnEventResponse> tokensWithdrawnEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getTokensWithdrawnEventFromLog(log));
    }

    public Flowable<TokensWithdrawnEventResponse> tokensWithdrawnEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(TOKENSWITHDRAWN_EVENT));
        return tokensWithdrawnEventFlowable(filter);
    }

    public static List<UserRegisteredEventResponse> getUserRegisteredEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(USERREGISTERED_EVENT, transactionReceipt);
        ArrayList<UserRegisteredEventResponse> responses = new ArrayList<UserRegisteredEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            UserRegisteredEventResponse typedResponse = new UserRegisteredEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.user = (String) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static UserRegisteredEventResponse getUserRegisteredEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(USERREGISTERED_EVENT, log);
        UserRegisteredEventResponse typedResponse = new UserRegisteredEventResponse();
        typedResponse.log = log;
        typedResponse.user = (String) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<UserRegisteredEventResponse> userRegisteredEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getUserRegisteredEventFromLog(log));
    }

    public Flowable<UserRegisteredEventResponse> userRegisteredEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(USERREGISTERED_EVENT));
        return userRegisteredEventFlowable(filter);
    }

    public RemoteFunctionCall<String> admin() {
        final Function function = new Function(FUNC_ADMIN, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<String> allocationStakingContract() {
        final Function function = new Function(FUNC_ALLOCATIONSTAKINGCONTRACT, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<Boolean> checkParticipationSignature(byte[] signature, String user,
            BigInteger amount) {
        final Function function = new Function(FUNC_CHECKPARTICIPATIONSIGNATURE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.DynamicBytes(signature), 
                new org.web3j.abi.datatypes.Address(160, user), 
                new org.web3j.abi.datatypes.generated.Uint256(amount)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<Boolean> checkRegistrationSignature(byte[] signature, String user) {
        final Function function = new Function(FUNC_CHECKREGISTRATIONSIGNATURE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.DynamicBytes(signature), 
                new org.web3j.abi.datatypes.Address(160, user)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<TransactionReceipt> depositTokens() {
        final Function function = new Function(
                FUNC_DEPOSITTOKENS, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> extendRegistrationPeriod(BigInteger timeToAdd) {
        final Function function = new Function(
                FUNC_EXTENDREGISTRATIONPERIOD, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(timeToAdd)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> factory() {
        final Function function = new Function(FUNC_FACTORY, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<BigInteger> getNumberOfRegisteredUsers() {
        final Function function = new Function(FUNC_GETNUMBEROFREGISTEREDUSERS, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<Tuple4<BigInteger, BigInteger, BigInteger, List<Boolean>>> getParticipation(
            String _user) {
        final Function function = new Function(FUNC_GETPARTICIPATION, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _user)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<DynamicArray<Bool>>() {}));
        return new RemoteFunctionCall<Tuple4<BigInteger, BigInteger, BigInteger, List<Boolean>>>(function,
                new Callable<Tuple4<BigInteger, BigInteger, BigInteger, List<Boolean>>>() {
                    @Override
                    public Tuple4<BigInteger, BigInteger, BigInteger, List<Boolean>> call() throws
                            Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple4<BigInteger, BigInteger, BigInteger, List<Boolean>>(
                                (BigInteger) results.get(0).getValue(), 
                                (BigInteger) results.get(1).getValue(), 
                                (BigInteger) results.get(2).getValue(), 
                                convertToNative((List<Bool>) results.get(3).getValue()));
                    }
                });
    }

    public RemoteFunctionCall<String> getParticipationSigner(byte[] signature, String user,
            BigInteger amount) {
        final Function function = new Function(FUNC_GETPARTICIPATIONSIGNER, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.DynamicBytes(signature), 
                new org.web3j.abi.datatypes.Address(160, user), 
                new org.web3j.abi.datatypes.generated.Uint256(amount)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<Tuple2<List<BigInteger>, List<BigInteger>>> getVestingInfo() {
        final Function function = new Function(FUNC_GETVESTINGINFO, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Uint256>>() {}, new TypeReference<DynamicArray<Uint256>>() {}));
        return new RemoteFunctionCall<Tuple2<List<BigInteger>, List<BigInteger>>>(function,
                new Callable<Tuple2<List<BigInteger>, List<BigInteger>>>() {
                    @Override
                    public Tuple2<List<BigInteger>, List<BigInteger>> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<List<BigInteger>, List<BigInteger>>(
                                convertToNative((List<Uint256>) results.get(0).getValue()), 
                                convertToNative((List<Uint256>) results.get(1).getValue()));
                    }
                });
    }

    public RemoteFunctionCall<Boolean> isParticipated(String param0) {
        final Function function = new Function(FUNC_ISPARTICIPATED, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<Boolean> isRegistered(String param0) {
        final Function function = new Function(FUNC_ISREGISTERED, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<BigInteger> maxVestingTimeShift() {
        final Function function = new Function(FUNC_MAXVESTINGTIMESHIFT, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<BigInteger> numberOfParticipants() {
        final Function function = new Function(FUNC_NUMBEROFPARTICIPANTS, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> participate(byte[] signature, BigInteger amount,
            BigInteger weiValue) {
        final Function function = new Function(
                FUNC_PARTICIPATE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.DynamicBytes(signature), 
                new org.web3j.abi.datatypes.generated.Uint256(amount)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<BigInteger> portionVestingPrecision() {
        final Function function = new Function(FUNC_PORTIONVESTINGPRECISION, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> postponeSale(BigInteger timeToShift) {
        final Function function = new Function(
                FUNC_POSTPONESALE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(timeToShift)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> registerForSale(byte[] signature,
            BigInteger pid) {
        final Function function = new Function(
                FUNC_REGISTERFORSALE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.DynamicBytes(signature), 
                new org.web3j.abi.datatypes.generated.Uint256(pid)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Tuple3<BigInteger, BigInteger, BigInteger>> registration() {
        final Function function = new Function(FUNC_REGISTRATION, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        return new RemoteFunctionCall<Tuple3<BigInteger, BigInteger, BigInteger>>(function,
                new Callable<Tuple3<BigInteger, BigInteger, BigInteger>>() {
                    @Override
                    public Tuple3<BigInteger, BigInteger, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple3<BigInteger, BigInteger, BigInteger>(
                                (BigInteger) results.get(0).getValue(), 
                                (BigInteger) results.get(1).getValue(), 
                                (BigInteger) results.get(2).getValue());
                    }
                });
    }

    public RemoteFunctionCall<Tuple14<String, Boolean, Boolean, Boolean, Boolean, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>> sale(
            ) {
        final Function function = new Function(FUNC_SALE, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Bool>() {}, new TypeReference<Bool>() {}, new TypeReference<Bool>() {}, new TypeReference<Bool>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        return new RemoteFunctionCall<Tuple14<String, Boolean, Boolean, Boolean, Boolean, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>>(function,
                new Callable<Tuple14<String, Boolean, Boolean, Boolean, Boolean, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>>() {
                    @Override
                    public Tuple14<String, Boolean, Boolean, Boolean, Boolean, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger> call(
                            ) throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple14<String, Boolean, Boolean, Boolean, Boolean, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>(
                                (String) results.get(0).getValue(), 
                                (Boolean) results.get(1).getValue(), 
                                (Boolean) results.get(2).getValue(), 
                                (Boolean) results.get(3).getValue(), 
                                (Boolean) results.get(4).getValue(), 
                                (String) results.get(5).getValue(), 
                                (BigInteger) results.get(6).getValue(), 
                                (BigInteger) results.get(7).getValue(), 
                                (BigInteger) results.get(8).getValue(), 
                                (BigInteger) results.get(9).getValue(), 
                                (BigInteger) results.get(10).getValue(), 
                                (BigInteger) results.get(11).getValue(), 
                                (BigInteger) results.get(12).getValue(), 
                                (BigInteger) results.get(13).getValue());
                    }
                });
    }

    public RemoteFunctionCall<TransactionReceipt> setCap(BigInteger cap) {
        final Function function = new Function(
                FUNC_SETCAP, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(cap)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> setRegistrationTime(
            BigInteger _registrationTimeStarts, BigInteger _registrationTimeEnds) {
        final Function function = new Function(
                FUNC_SETREGISTRATIONTIME, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_registrationTimeStarts), 
                new org.web3j.abi.datatypes.generated.Uint256(_registrationTimeEnds)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> setSaleParams(String _token, String _saleOwner,
            BigInteger _tokenPriceInETH, BigInteger _amountOfTokensToSell, BigInteger _saleEnd,
            BigInteger _tokensUnlockTime, BigInteger _portionVestingPrecision,
            BigInteger _maxParticipation) {
        final Function function = new Function(
                FUNC_SETSALEPARAMS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _token), 
                new org.web3j.abi.datatypes.Address(160, _saleOwner), 
                new org.web3j.abi.datatypes.generated.Uint256(_tokenPriceInETH), 
                new org.web3j.abi.datatypes.generated.Uint256(_amountOfTokensToSell), 
                new org.web3j.abi.datatypes.generated.Uint256(_saleEnd), 
                new org.web3j.abi.datatypes.generated.Uint256(_tokensUnlockTime), 
                new org.web3j.abi.datatypes.generated.Uint256(_portionVestingPrecision), 
                new org.web3j.abi.datatypes.generated.Uint256(_maxParticipation)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> setSaleStart(BigInteger starTime) {
        final Function function = new Function(
                FUNC_SETSALESTART, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(starTime)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> setSaleToken(String saleToken) {
        final Function function = new Function(
                FUNC_SETSALETOKEN, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, saleToken)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> setVestingParams(List<BigInteger> _unlockingTimes,
            List<BigInteger> _percents, BigInteger _maxVestingTimeShift) {
        final Function function = new Function(
                FUNC_SETVESTINGPARAMS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.generated.Uint256>(
                        org.web3j.abi.datatypes.generated.Uint256.class,
                        org.web3j.abi.Utils.typeMap(_unlockingTimes, org.web3j.abi.datatypes.generated.Uint256.class)), 
                new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.generated.Uint256>(
                        org.web3j.abi.datatypes.generated.Uint256.class,
                        org.web3j.abi.Utils.typeMap(_percents, org.web3j.abi.datatypes.generated.Uint256.class)), 
                new org.web3j.abi.datatypes.generated.Uint256(_maxVestingTimeShift)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> shiftVestingUnlockingTimes(
            BigInteger timeToShift) {
        final Function function = new Function(
                FUNC_SHIFTVESTINGUNLOCKINGTIMES, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(timeToShift)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> updateTokenPriceInETH(BigInteger price) {
        final Function function = new Function(
                FUNC_UPDATETOKENPRICEINETH, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(price)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Tuple3<BigInteger, BigInteger, BigInteger>> userToParticipation(
            String param0) {
        final Function function = new Function(FUNC_USERTOPARTICIPATION, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        return new RemoteFunctionCall<Tuple3<BigInteger, BigInteger, BigInteger>>(function,
                new Callable<Tuple3<BigInteger, BigInteger, BigInteger>>() {
                    @Override
                    public Tuple3<BigInteger, BigInteger, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple3<BigInteger, BigInteger, BigInteger>(
                                (BigInteger) results.get(0).getValue(), 
                                (BigInteger) results.get(1).getValue(), 
                                (BigInteger) results.get(2).getValue());
                    }
                });
    }

    public RemoteFunctionCall<BigInteger> vestingPercentPerPortion(BigInteger param0) {
        final Function function = new Function(FUNC_VESTINGPERCENTPERPORTION, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<BigInteger> vestingPortionsUnlockTime(BigInteger param0) {
        final Function function = new Function(FUNC_VESTINGPORTIONSUNLOCKTIME, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> withdrawEarnings() {
        final Function function = new Function(
                FUNC_WITHDRAWEARNINGS, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> withdrawEarningsAndLeftover() {
        final Function function = new Function(
                FUNC_WITHDRAWEARNINGSANDLEFTOVER, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> withdrawLeftover() {
        final Function function = new Function(
                FUNC_WITHDRAWLEFTOVER, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> withdrawMultiplePortions(
            List<BigInteger> portionIds) {
        final Function function = new Function(
                FUNC_WITHDRAWMULTIPLEPORTIONS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.generated.Uint256>(
                        org.web3j.abi.datatypes.generated.Uint256.class,
                        org.web3j.abi.Utils.typeMap(portionIds, org.web3j.abi.datatypes.generated.Uint256.class))), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> withdrawTokens(BigInteger portionId) {
        final Function function = new Function(
                FUNC_WITHDRAWTOKENS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(portionId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static C2NSale load(String contractAddress, Web3j web3j, Credentials credentials,
                               BigInteger gasPrice, BigInteger gasLimit) {
        return new C2NSale(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static C2NSale load(String contractAddress, Web3j web3j,
                               TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new C2NSale(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static C2NSale load(String contractAddress, Web3j web3j, Credentials credentials,
                               ContractGasProvider contractGasProvider) {
        return new C2NSale(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static C2NSale load(String contractAddress, Web3j web3j,
                               TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new C2NSale(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static class MaxParticipationSetEventResponse extends BaseEventResponse {
        public BigInteger maxParticipation;
    }

    public static class RegistrationTimeSetEventResponse extends BaseEventResponse {
        public BigInteger registrationTimeStarts;

        public BigInteger registrationTimeEnds;
    }

    public static class SaleCreatedEventResponse extends BaseEventResponse {
        public String saleOwner;

        public BigInteger tokenPriceInETH;

        public BigInteger amountOfTokensToSell;

        public BigInteger saleEnd;

        public BigInteger tokensUnlockTime;

        public BigInteger registrationStart;

        public BigInteger registrationEnd;

        public BigInteger saleStart;
    }

    public static class StartTimeSetEventResponse extends BaseEventResponse {
        public BigInteger startTime;
    }

    public static class TokenPriceSetEventResponse extends BaseEventResponse {
        public BigInteger newPrice;
    }

    public static class TokensSoldEventResponse extends BaseEventResponse {
        public String user;

        public BigInteger amount;
    }

    public static class TokensWithdrawnEventResponse extends BaseEventResponse {
        public String user;

        public BigInteger amount;
    }

    public static class UserRegisteredEventResponse extends BaseEventResponse {
        public String user;
    }
}
