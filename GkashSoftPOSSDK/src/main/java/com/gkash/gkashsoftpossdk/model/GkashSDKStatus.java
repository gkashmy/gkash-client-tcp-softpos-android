package com.gkash.gkashsoftpossdk.model;

import android.util.Log;

import com.gkash.gkashsoftpossdk.GkashSoftPOSSDK;

public class GkashSDKStatus {
    private GkashSoftPOSSDK.SocketConnectivityCallback mSocketStatus = GkashSoftPOSSDK.SocketConnectivityCallback.DEFAULT;
    private GkashSoftPOSSDK.TransactionEventCallback mTransactionEventCallback = GkashSoftPOSSDK.TransactionEventCallback.DEFAULT;
    private GkashSoftPOSSDK.PaymentType mTransactionType = GkashSoftPOSSDK.PaymentType.CARD_PAYMENT;
    private final GkashSoftPOSSDK.GkashStatusCallback mGkashStatusCallback;
    private final String logEvent = "GkashSDKStatus";

    public  GkashSDKStatus(GkashSoftPOSSDK.GkashStatusCallback gkashStatusCallback){
        mGkashStatusCallback = gkashStatusCallback;
    }

    public GkashSoftPOSSDK.SocketConnectivityCallback getSocketStatus() {
        return mSocketStatus;
    }

    public void setSocketStatus(GkashSoftPOSSDK.SocketConnectivityCallback socketStatus) {
        Log.d(logEvent, "setTransactionType: " + socketStatus.name());
        this.mSocketStatus = socketStatus;
        mGkashStatusCallback.SocketStatus(socketStatus);
    }

    public GkashSoftPOSSDK.TransactionEventCallback getTransactionEventCallback() {
        return mTransactionEventCallback;
    }

    public void setTransactionEventCallback(GkashSoftPOSSDK.TransactionEventCallback transactionEventCallback) {
        Log.d(logEvent, "setTransactionType: " + transactionEventCallback.name());
        this.mTransactionEventCallback = transactionEventCallback;
        mGkashStatusCallback.TransactionEvent(transactionEventCallback);
    }

    public GkashSoftPOSSDK.PaymentType getTransactionType() {
        return mTransactionType;
    }

    public void setTransactionType(GkashSoftPOSSDK.PaymentType transactionType) {
        Log.d(logEvent, "setTransactionType: " + transactionType.name());
        this.mTransactionType = transactionType;
    }
}
