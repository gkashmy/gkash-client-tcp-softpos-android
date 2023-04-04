package com.gkash.gkashsoftpossdk.service;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.gkash.gkashsoftpossdk.GkashSoftPOSSDK;
import com.gkash.gkashsoftpossdk.model.PaymentRequestDto;
import com.gkash.gkashsoftpossdk.model.SocketEventCallback;
import com.gkash.gkashsoftpossdk.model.GkashSDKStatus;
import com.gkash.gkashsoftpossdk.model.TransactionDetails;
import com.gkash.gkashsoftpossdk.model.TransactionResult;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class GkashSocketThread extends Thread {

    private static final int SERVER_PORT = 38300;
    private static final String pfxPassword = "9pcTOBjq";
    private final PaymentRequestDto requestDto;
    private boolean running = true;
    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    private final String logEvent = "GkashSDKThread";
    private final Handler handler = new Handler();
    private Boolean queryingStatus = false;
    private final GkashSDKStatus mGkashSDKStatus;
    private final GkashSoftPOSSDK mSdk;
    private int _pingCount = 0;
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            String data = String.valueOf(GkashSoftPOSSDK.TransactionEventCallback.CHECK_CONNECTION.ordinal());
            sendMessage(data);
            Log.d(logEvent, "CHECK_CONNECTION");
            if(queryingStatus){
                Log.d(logEvent, "queryingStatus true");
                handler.postDelayed(this, 2000);
            }
        }
    };
    private final Handler checkInternetHandler = new Handler();
    private final Runnable checkInternetRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(logEvent, "run checkInternet");
            boolean isActive =  checkInternet();
            Log.d(logEvent, "internet: " + isActive);
            if(!isActive){
                checkInternetHandler.postDelayed(this, 2000);
            }else{
                checkInternetHandler.removeCallbacks(this);
                Log.d(logEvent, "try reconnect");
                reconnect();
                return;
            }

            if(_pingCount > 10){
                checkInternetHandler.removeCallbacks(this);
            }
        }
    };

    public GkashSocketThread (PaymentRequestDto dto, GkashSDKStatus gkashSDKStatus){
        this.requestDto = dto;
        this.mGkashSDKStatus = gkashSDKStatus;
        this.mSdk = GkashSoftPOSSDK.getInstance();
        Log.d(logEvent, "GkashSocketThread");
    }

    @Override
    public void run() {
        try {
            Log.d(logEvent, "GkashSocketThread run");
            File root = Environment.getExternalStorageDirectory();

            String serverHost = mSdk.getIpAddress();
            // Construct the path to the folder and file
            String path = root + mSdk.getCertPath();
            // Load the TLS certificate from the file
            FileInputStream certInputStream = new FileInputStream(path);
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(certInputStream, pfxPassword.toCharArray());

            // Create a TrustManager that trusts the server's certificate
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

            // Create a KeyManager that presents the client's certificate
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, pfxPassword.toCharArray());
            KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

            // Create an SSLContext that uses the specified trust and key managers
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, trustManagers, null);

            // Connect to the server using the SSLContext
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            Socket socket = sslSocketFactory.createSocket(serverHost, SERVER_PORT);

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            OutputStream outputStream = socket.getOutputStream();

            Log.d(logEvent, "connected");
            mGkashSDKStatus.setSocketStatus(GkashSoftPOSSDK.SocketConnectivityCallback.CONNECTED);

            // Start the message loop
            while (running) {
                try {
                    // Take the next message from the queue and send it to the server
                    String jsonString = messageQueue.take();
                    outputStream.write(jsonString.getBytes());
                    Log.d(logEvent, "sent: " + jsonString);
                    String message = "";

                    char[] buffer = new char[1024];
                    int bytesRead;
                    StringBuilder messageBuilder = new StringBuilder();
                    while ((bytesRead = reader.read(buffer)) != -1) {
                        messageBuilder.append(buffer, 0, bytesRead);
                        message = messageBuilder.toString();
                        Log.d(logEvent, "Received data[0]: " + message);
                        if(message.contains("<EOF>")){
                            message = message.replace("<EOF>", "");
                            break;
                        }
                    }

                    Log.d(logEvent, "Received data: " + message);
                    if (!message.equals("")) {
                        // do something with the message
                        JSONObject jsonObject = new JSONObject(message);
                        SocketEventCallback callback = new SocketEventCallback();

                        int type =  jsonObject.getInt("type");
                        callback.setType(GkashSoftPOSSDK.TransactionCallbackType.values()[type]);

                        if(callback.getType() == GkashSoftPOSSDK.TransactionCallbackType.TRANSACTION_STATUS){

                            int eventIndex =  jsonObject.getInt("eventIndex");
                            callback.setEventIndex(GkashSoftPOSSDK.TransactionEventCallback.values()[eventIndex]);

                            Log.d(logEvent, GkashSoftPOSSDK.TransactionCallbackType.TRANSACTION_STATUS.name());
                            Log.d(logEvent, callback.getEventIndex().name());

                            mGkashSDKStatus.setTransactionEventCallback(callback.getEventIndex());

                            if(callback.getEventIndex() == GkashSoftPOSSDK.TransactionEventCallback.INIT_PAYMENT){
                                startQuery();
                            }else if(callback.getEventIndex() == GkashSoftPOSSDK.TransactionEventCallback.CANCEL_PAYMENT ||
                                     callback.getEventIndex() == GkashSoftPOSSDK.TransactionEventCallback.DONE ||
                                     callback.getEventIndex() == GkashSoftPOSSDK.TransactionEventCallback.INVALID_METHOD ||
                                     callback.getEventIndex() == GkashSoftPOSSDK.TransactionEventCallback.CHECK_TERMINAL_STATUS ||
                                     callback.getEventIndex() == GkashSoftPOSSDK.TransactionEventCallback.NO_CARD_DETECTED_TIMEOUT ||
                                     callback.getEventIndex() == GkashSoftPOSSDK.TransactionEventCallback.GET_KEY_FAIL ||
                                     callback.getEventIndex() == GkashSoftPOSSDK.TransactionEventCallback.INVALID_SIGNATURE ||
                                     callback.getEventIndex() == GkashSoftPOSSDK.TransactionEventCallback.DEVICE_OFFLINE ||
                                     callback.getEventIndex() == GkashSoftPOSSDK.TransactionEventCallback.INVALID_AMOUNT ||
                                     callback.getEventIndex() == GkashSoftPOSSDK.TransactionEventCallback.DEFAULT){

                                stopQuery();
                                stopRunning();
                                mGkashSDKStatus.setTransactionEventCallback(callback.getEventIndex());
                            }
                        }else if (callback.getType() == GkashSoftPOSSDK.TransactionCallbackType.TRANSACTION_RESULT){
                            Log.d(logEvent, GkashSoftPOSSDK.TransactionCallbackType.TRANSACTION_RESULT.name());
                            stopQuery();

                            JSONObject resultObj = new JSONObject(message);
                            TransactionResult result = new TransactionResult();
                            result.setType(resultObj.getString("type"));

                            JSONObject resultJsonObject = resultObj.getJSONObject("result");
                            TransactionDetails transactionDetails = new TransactionDetails();
                            transactionDetails.setApplicationId(resultJsonObject.getString("ApplicationId"));
                            transactionDetails.setAuthIDResponse(resultJsonObject.getString("AuthIDResponse"));
                            transactionDetails.setResponseOrderNumber(resultJsonObject.getString("ResponseOrderNumber"));
                            transactionDetails.setCardNo(resultJsonObject.getString("CardNo"));
                            transactionDetails.setCardType(resultJsonObject.getString("CardType"));
                            transactionDetails.setCartID(resultJsonObject.getString("CartID"));
                            transactionDetails.setCompanyRemID(resultJsonObject.getString("CompanyRemID"));
                            transactionDetails.setMID(resultJsonObject.getString("MID"));
                            transactionDetails.setMessage(resultJsonObject.getString("Message"));
                            transactionDetails.setMethod(resultJsonObject.getString("Method"));
                            transactionDetails.setRemID(resultJsonObject.getString("RemID"));
                            transactionDetails.setSettlementBatchNumber(resultJsonObject.getString("SettlementBatchNumber"));
                            transactionDetails.setSignatureRequired(resultJsonObject.getString("SignatureRequired"));
                            transactionDetails.setStatus(resultJsonObject.getString("Status"));
                            transactionDetails.setTID(resultJsonObject.getString("TID"));
                            transactionDetails.setTVR(resultJsonObject.getString("TVR"));
                            transactionDetails.setTraceNo(resultJsonObject.getString("TraceNo"));
                            transactionDetails.setTransferAmount(resultJsonObject.getString("TransferAmount"));
                            transactionDetails.setTransferCurrency(resultJsonObject.getString("TransferCurrency"));
                            transactionDetails.setTransferDate(resultJsonObject.getString("TransferDate"));
                            transactionDetails.setTxType(resultJsonObject.getString("TxType"));
                            transactionDetails.setSignature(resultJsonObject.getString("Signature"));

                            result.setResult(transactionDetails);

                            mSdk.SendTransactionResult(transactionDetails);
                            stopRunning();
                        }
                    }
                } catch (InterruptedException e) {
                    Log.d(logEvent, "InterruptedException: " + e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }

            // close the socket and streams
            outputStream.close();
            Log.d(logEvent, "outputStream close");
            reader.close();
            Log.d(logEvent, "reader close");
            socket.close();
            Log.d(logEvent, "socket close");
            stopQuery();
        } catch (Exception e) {
            Log.d(logEvent, "Run Exception: " + e.getMessage());
            Log.d(logEvent, e.getMessage());

            stopQuery();
            stopRunning();

            String errorMessage = e.getMessage();
            if(errorMessage != null && errorMessage.contains("Permission denied")){
                mGkashSDKStatus.setSocketStatus(GkashSoftPOSSDK.SocketConnectivityCallback.AUTH_ERROR);
                return;
            }

            startCheckInternet();
        }
    }

    public static String getSha512Hash(List<String> listToHash) {

        String stringToHash = "";
        String sep = "";

        for (String s : listToHash) {
            stringToHash += sep + s;
            sep = ";";
        }
        stringToHash = stringToHash.toUpperCase();

        String hash = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] bytes = md.digest(stringToHash.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            hash = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hash;
    }

    public void startQuery(){
        if(!queryingStatus){
            queryingStatus = true;
            Log.d(logEvent, "Init query status");
            handler.postDelayed(runnable, 2000);
        }
    }

    private void stopQuery(){
        Log.d(logEvent, "stopQuery");
        if(queryingStatus){
            queryingStatus = false;
            handler.removeCallbacks(runnable);
            Log.d(logEvent, "queryingStatus false");
        }
    }

    public void sendPaymentRequest(){
        List<String> listToHash = new ArrayList<>();
        listToHash.add(mSdk.getSignatureKey());
        listToHash.add(requestDto.getAmount().replace(".", ""));
        listToHash.add(requestDto.getEmail());
        listToHash.add(requestDto.getReferenceNo());
        listToHash.add(requestDto.getMobileNo());
        listToHash.add(String.valueOf(requestDto.getPaymentType().ordinal()));
        listToHash.add(Boolean.toString(requestDto.isPreAuth()));
        requestDto.setSignature(getSha512Hash(listToHash));
        String requestString = mSdk.toJsonString(requestDto);
        mGkashSDKStatus.setTransactionType(requestDto.getPaymentType());
        sendMessage(requestString);
    }

    private void sendMessage(String jsonString) {
        // Add the message to the queue
        messageQueue.add(jsonString);
    }

    private void stopRunning() {
        Log.d(logEvent, "stopRunning");
        mGkashSDKStatus.setSocketStatus(GkashSoftPOSSDK.SocketConnectivityCallback.DISCONNECTED);
        running = false;
    }

    private void reconnect(){
        Log.d(logEvent, "reconnect");
        mGkashSDKStatus.setSocketStatus(GkashSoftPOSSDK.SocketConnectivityCallback.RECONNECTING);
        if(running){
            stopRunning();
        }

        this.interrupt();
        mSdk.rerunSocket();
    }

    private void startCheckInternet(){
        Log.d(logEvent, "startCheckInternet");
        checkInternetHandler.postDelayed(checkInternetRunnable, 2000);
    }

    private boolean checkInternet(){
        try {
            _pingCount ++;
            String pingGoogleDNS = "ping -c 1 8.8.8.8";
            String pingHost = "ping -c 1 " + mSdk.getIpAddress();

            boolean pingHostSuccess = (Runtime.getRuntime().exec(pingHost).waitFor() == 0);
            boolean pingGoogleSuccess = (Runtime.getRuntime().exec(pingGoogleDNS).waitFor() == 0);
            Log.d(logEvent, "pingHost: " + mSdk.getIpAddress() + " " + pingHostSuccess);
            Log.d(logEvent, "pingGoogle: " + pingGoogleSuccess);
            //Ping host successful, ready to reconnect
            if(pingHostSuccess){
                _pingCount = 0;
                return true;
            }else{
                //Ping host unsuccessful but have active internet, update IpAddress and ping
                if(pingGoogleSuccess){
                    if(_pingCount > 10){
                        Log.d(logEvent, "unable to ping host and google");
                    }
                    mSdk.retrieveIpAddress();
                }else{
                    //stop ping and return try again status to client
                    if(_pingCount > 10){
                        Log.d(logEvent, "unable to ping host and google");
                        mGkashSDKStatus.setSocketStatus(GkashSoftPOSSDK.SocketConnectivityCallback.DISCONNECTED);
                        mGkashSDKStatus.setTransactionEventCallback(GkashSoftPOSSDK.TransactionEventCallback.TRY_AGAIN);
                    }
                }
                return false;
            }
        } catch (Exception e) {
            mGkashSDKStatus.setSocketStatus(GkashSoftPOSSDK.SocketConnectivityCallback.DISCONNECTED);
            mGkashSDKStatus.setTransactionEventCallback(GkashSoftPOSSDK.TransactionEventCallback.TRY_AGAIN);
            return false;
        }
    }
}
