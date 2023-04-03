package com.gkash.gkashsoftpossdk;

import android.util.Log;

import com.gkash.gkashsoftpossdk.model.GkashSDKConfig;
import com.gkash.gkashsoftpossdk.model.IpAddressDto;
import com.gkash.gkashsoftpossdk.model.PaymentRequestDto;
import com.gkash.gkashsoftpossdk.model.GkashSDKStatus;
import com.gkash.gkashsoftpossdk.model.TransactionDetails;
import com.gkash.gkashsoftpossdk.model.UserloginDto;
import com.gkash.gkashsoftpossdk.service.GkashHttpClient;
import com.gkash.gkashsoftpossdk.service.GkashSocketThread;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidParameterException;


public final class GkashSoftPOSSDK {
    private static GkashSoftPOSSDK mInstance;
    private PaymentRequestDto paymentRequestDto;
    private final String logEvent = " ";
    private GkashStatusCallback gkashStatusCallback;
    private GkashSDKStatus mGkashSDKStatus;
    private GkashSDKConfig sdkConfig;
    private final GkashHttpClient gkashHttpClient = new GkashHttpClient();
    private String mAuthToken = "", mIpAddress = "", mSignatureKey = "";
    public static final String VERSION = "1.0.0";
    private GkashSocketThread gkashSocketThread;
    private PaymentRequestDto requestDto;
    private Thread apiThread;

    public interface GkashStatusCallback{
        void TransactionResult(TransactionDetails details);
        void SocketStatus(SocketConnectivityCallback connectivityCallback);
        void TransactionEvent(TransactionEventCallback transactionEventCallback);
        void QueryTransactionResult(TransactionDetails details);
    }

    public static synchronized GkashSoftPOSSDK getInstance() {
        if(mInstance == null){
            mInstance = new GkashSoftPOSSDK();
        }
       return mInstance;
    }

    public GkashSoftPOSSDK() {

    }

    public void init(GkashSDKConfig sdkConfig, GkashStatusCallback callback){
        Log.d(logEvent, "Gkash SDK init");
        gkashStatusCallback = callback;
        mGkashSDKStatus = new GkashSDKStatus(callback);
        this.sdkConfig = sdkConfig;
        if(sdkConfig.isTestingEnvironment()){
            gkashHttpClient.setHOST_URL("https://api-staging.pay.asia");
        }else{
            gkashHttpClient.setHOST_URL("https://api.gkash.my");
        }

        if(apiThread != null){
            apiThread.interrupt();
        }

        apiThread = new Thread(() -> {
            mAuthToken = login();

            if(mAuthToken == null){
                mGkashSDKStatus.setSocketStatus(SocketConnectivityCallback.LOGIN_FAIL);
                return;
            }
            Log.d(logEvent, "key: " + mAuthToken);
            retrieveKey();

            if(mSignatureKey == null){
                mGkashSDKStatus.setSocketStatus(SocketConnectivityCallback.RETRIEVE_KEY_FAIL);
                return;
            }
            Log.d(logEvent, "key: " + mSignatureKey);
            retrieveIpAddress();

            if(mIpAddress == null){
                mGkashSDKStatus.setSocketStatus(SocketConnectivityCallback.RETRIEVE_IP_FAIL);
                return;
            }
            Log.d(logEvent, "ipAddress: " + mIpAddress);
            mGkashSDKStatus.setSocketStatus(SocketConnectivityCallback.ONLINE);
        });
        apiThread.start();
    }

    private String login(){
        UserloginDto loginDto = new UserloginDto();
        loginDto.setUsername(sdkConfig.getUsername());
        loginDto.setPassword(sdkConfig.getPassword());
        String jsonString = toJsonString(loginDto);
        Log.d(logEvent, "login API");
        return gkashHttpClient.login(jsonString);
    }

    private void retrieveKey(){
        Log.d(logEvent, "getKey API");
        mSignatureKey = gkashHttpClient.getSignatureKey(mAuthToken);
    }

    public void retrieveIpAddress(){
        IpAddressDto addressDto = new IpAddressDto();
        addressDto.setAuthToken(mAuthToken);
        addressDto.setRemID(sdkConfig.getUsername());
        String jsonString = toJsonString(addressDto);
        Log.d(logEvent, "getIpAddress API");
        mIpAddress = gkashHttpClient.getIpAddress(jsonString);
    }

    public String getIpAddress() {
        return mIpAddress;
    }

    public String getCertPath(){
        return this.sdkConfig.getCertPath();
    }

    public String getSignatureKey() {
        return mSignatureKey;
    }

    public void SendTransactionResult(TransactionDetails details){
     //   TransactionDetails result =  StringToTransactionResult(jsonString);
        gkashStatusCallback.TransactionResult(details);
    }

    public void requestPayment(PaymentRequestDto requestDto){
        this.requestDto = requestDto;
        Log.d(logEvent, "requestPayment");

        if(requestDto.getAmount() == null){
            throw new IllegalArgumentException("Amount cannot be null");
        }
        try{
            double amount = Double.parseDouble(requestDto.getAmount());
            if(amount == 0.00){
                throw new IllegalArgumentException("Invalid amount");
            }
        }catch (NumberFormatException e){
            throw new IllegalArgumentException("Invalid amount");
        }


        if(requestDto.getPaymentType() == null){
            throw new IllegalArgumentException("Payment type cannot be null");
        }

        if(mGkashSDKStatus.getSocketStatus() != SocketConnectivityCallback.CONNECTED){
            Log.d(logEvent, "start");
            gkashSocketThread = new GkashSocketThread(requestDto, mGkashSDKStatus);
            Log.d(logEvent, gkashSocketThread.getState().name());
            gkashSocketThread.start();

            gkashSocketThread.sendPaymentRequest();
        }
    }

    public void queryTransactionStatus(String referenceNo){
        if(apiThread != null){
            apiThread.interrupt();
        }

        apiThread = new Thread(() -> {
            if(!mAuthToken.isEmpty()){
                TransactionDetails details = gkashHttpClient.queryStatus(referenceNo, mAuthToken);
                if(details != null){
                    gkashStatusCallback.QueryTransactionResult(details);
                }
            }else{
                mGkashSDKStatus.setSocketStatus(SocketConnectivityCallback.LOGIN_FAIL);
            }

        });

        apiThread.start();
    }

    public void rerunSocket(){
        gkashSocketThread.interrupt();
        gkashSocketThread = new GkashSocketThread(requestDto, mGkashSDKStatus);
        gkashSocketThread.start();
        gkashSocketThread.startQuery();
    }

    public String toJsonString(Object obj) {
        JSONObject jsonObject = new JSONObject();

        try {
            // Get all fields of the class
            java.lang.reflect.Field[] fields = obj.getClass().getDeclaredFields();
            for (java.lang.reflect.Field field : fields) {
                // Make fields accessible to get their values
                field.setAccessible(true);
                // Add field name and value to JSONObject
                Object value = field.get(obj);
                if(field.getName().equals("PaymentType")){
                    GkashSoftPOSSDK.PaymentType type = (GkashSoftPOSSDK.PaymentType) field.get(obj);
                    value = type != null ? type.ordinal() : 0;
                }

                jsonObject.put(field.getName(), value);
            }
        } catch (IllegalAccessException | JSONException e) {
            e.printStackTrace();
        }

        return jsonObject.toString();
    }

    public enum PaymentType
    {
        EWALLET_SCAN_PAYMENT,
        TAPTOPHONE_PAYMENT,
        CARD_PAYMENT,
        MAYBANK_QR,
        GRABPAY_QR,
        TNG_QR,
        GKASH_QR,
        BOOST_QR,
        WECHAT_QR,
        SHOPEE_PAY_QR,
        ALIPAY_QR,
        ATOME_QR,
        MCASH_QR,
        DUITNOW_QR
    }

    public enum TransactionEventCallback
    {
        CONNECTION_OK,
        CHECK_CONNECTION,
        INIT_PAYMENT,
        TRANSACTION_PROCESSING,
        DISPLAYING_QR,
        SCANNING_QR,
        READY_TO_READ_CARD,
        INPUT_PIN,
        RETRIEVING_PAYMENT_STATUS,
        RETRIEVED_STATUS,
        QUERY_STATUS,
        INVALID_SIGNATURE,
        CANCEL_PAYMENT,
        INVALID_PAYMENT_TYPE,
        INVALID_METHOD,
        INVALID_AMOUNT,
        GET_KEY_FAIL,
        DEVICE_OFFLINE,
        NO_CARD_DETECTED_TIMEOUT,
        NO_PIN_DETECTED_TIMEOUT,
        CHECK_TERMINAL_STATUS,
        TRY_AGAIN,
        RECONNECTED,
        LAST_TRANS_STATUS,
        DONE,
        DEFAULT
    }

    public enum SocketConnectivityCallback
    {
        ONLINE,
        CONNECTED,
        DISCONNECTED,
        RECONNECTING,
        HOST_NOT_FOUND,
        LOGIN_FAIL,
        RETRIEVE_IP_FAIL,
        RETRIEVE_KEY_FAIL,
        AUTH_ERROR,
        DEFAULT
    }

    public enum TransactionCallbackType
    {
        TRANSACTION_RESULT,
        TRANSACTION_STATUS
    }
}