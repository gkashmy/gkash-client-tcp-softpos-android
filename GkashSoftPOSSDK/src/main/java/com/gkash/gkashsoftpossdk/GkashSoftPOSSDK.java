package com.gkash.gkashsoftpossdk;

import android.content.Context;
import android.util.Log;


import com.gkash.gkashsoftpossdk.model.PaymentRequestDto;
import com.gkash.gkashsoftpossdk.model.GkashSDKStatus;
import com.gkash.gkashsoftpossdk.model.TransactionDetails;
import com.gkash.gkashsoftpossdk.service.GkashSocketThread;

import org.json.JSONException;
import org.json.JSONObject;


public final class GkashSoftPOSSDK {
    private static GkashSoftPOSSDK mInstance;
    private PaymentRequestDto paymentRequestDto;
    private final Context applicationContext;
    private GkashSocketThread gkashSocketThread;
    private final String logEvent = "GkashSDKMain";
    private GkashStatusCallback gkashStatusCallback;
    private GkashSDKStatus mGkashSDKStatus;

    public interface GkashStatusCallback{
        void TransactionResult(TransactionDetails result);
        void SocketStatus(SocketConnectivityCallback connectivityCallback);
        void TransactionEvent(TransactionEventCallback transactionEventCallback);
    }

    public static synchronized GkashSoftPOSSDK getInstance(Context context) {
        if(mInstance == null){
            mInstance = new GkashSoftPOSSDK(context);
        }
       return mInstance;
    }

    private GkashSoftPOSSDK(Context context) {
        applicationContext = context.getApplicationContext();
    }

    public PaymentRequestDto getPaymentRequestDto() {
        return paymentRequestDto;
    }

    public void setPaymentRequestDto(PaymentRequestDto paymentRequestDto) {
        this.paymentRequestDto = paymentRequestDto;
    }

    public void SendTransactionResult(TransactionDetails details){
     //   TransactionDetails result =  StringToTransactionResult(jsonString);
        gkashStatusCallback.TransactionResult(details);
    }

    public void requestPayment(PaymentRequestDto requestDto, GkashStatusCallback callback){
        Log.d(logEvent, "requestPayment");
        mGkashSDKStatus = new GkashSDKStatus(callback);
        gkashStatusCallback = callback;
        Log.d(logEvent, "init");
        if(mGkashSDKStatus.getSocketStatus() != SocketConnectivityCallback.CONNECTED){
            Log.d(logEvent, "start");
            gkashSocketThread = new GkashSocketThread(requestDto, mGkashSDKStatus);
            Log.d(logEvent, gkashSocketThread.getState().name());
            gkashSocketThread.start();
        }
    }

//    public TransactionDetails StringToTransactionResult(String message){
//        JSONObject jsonObject = null;
//        try {
//            jsonObject = new JSONObject(message);
//            TransactionDetails transactionDetails = new TransactionDetails();
//            transactionDetails.setStatus(jsonObject.getString("Status"));
//            transactionDetails.setTransferAmount(jsonObject.getString("TransferAmount"));
//
//          return transactionDetails;
//        } catch (JSONException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }



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