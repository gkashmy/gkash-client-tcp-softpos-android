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

import org.json.JSONException;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class GkashSocketThread extends Thread {

    private Socket socket;
    private BufferedReader reader;
    private GkashSoftPOSSDK mSDK;
    private static final int SERVER_PORT = 38300;
    private final static String SERVER_HOST = "192.168.0.138";
    private static final String pfxPassword = "9pcTOBjq";
    private PaymentRequestDto requestDto;
    private boolean running = true;
    private OutputStream outputStream;
    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    private final String logEvent = "GkashSDKThread";
    private final Handler handler = new Handler();
    private Boolean queryingStatus = false;
    private GkashSDKStatus mGkashSDKStatus;
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


    public GkashSocketThread (PaymentRequestDto dto, GkashSDKStatus gkashSDKStatus){
        this.requestDto = dto;
        this.mGkashSDKStatus = gkashSDKStatus;
        Log.d(logEvent, "GkashSocketThread");
    }

    @Override
    public void run() {
        try {
            Log.d(logEvent, "GkashSocketThread run");
            mSDK = GkashSoftPOSSDK.getInstance(null);
            File root = Environment.getExternalStorageDirectory();
            String fileName = "t1clientcert.pfx";
            // Construct the path to the folder and file
            File folder = new File(root, "GkashSDKCert");
            File file = new File(folder, fileName);
            String path = file.getPath();
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
            socket = sslSocketFactory.createSocket(SERVER_HOST, SERVER_PORT);

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputStream = socket.getOutputStream();

            Log.d(logEvent, "connected");
            mGkashSDKStatus.setSocketStatus(GkashSoftPOSSDK.SocketConnectivityCallback.CONNECTED);

            List<String> listToHash = new ArrayList<>();
            listToHash.add("Daf1CrgnYPIz026");
            listToHash.add(requestDto.Amount.replace(".", ""));
            listToHash.add(requestDto.Email);
            listToHash.add(requestDto.ReferenceNo);
            listToHash.add(requestDto.MobileNo);
            listToHash.add(String.valueOf(requestDto.PaymentType.ordinal()));
            listToHash.add(Boolean.toString(requestDto.PreAuth));
            requestDto.Signature = getSha512Hash(listToHash);
            String requestString = toJsonString(requestDto);
            mGkashSDKStatus.setTransactionType(requestDto.PaymentType);
            sendMessage(requestString);

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
                            break;
                        }
                    }

                    if(message.contains("<EOF>")){
                        message = message.replace("<EOF>", "");
                    }

                    Log.d(logEvent, "Received data: " + message);
                    if (!message.equals("")) {
                        // do something with the message
                        JSONObject jsonObject = new JSONObject(message);
                        SocketEventCallback callback = new SocketEventCallback();
                        callback.setType(jsonObject.getInt("type"));

                        if(callback.getType() == GkashSoftPOSSDK.TransactionCallbackType.TRANSACTION_STATUS.ordinal()){
                            callback.setEventIndex(jsonObject.getInt("eventIndex"));
                            Log.d(logEvent, GkashSoftPOSSDK.TransactionCallbackType.TRANSACTION_STATUS.name());

                            Log.d(logEvent, GkashSoftPOSSDK.TransactionEventCallback.values()[callback.getEventIndex()].name());

                            if(callback.getEventIndex() == GkashSoftPOSSDK.TransactionEventCallback.INIT_PAYMENT.ordinal()){
                                if(!queryingStatus){
                                    queryingStatus = true;
                                    Log.d(logEvent, "Init query status");
                                    handler.postDelayed(runnable, 2000);
                                }
                            }else if(callback.getEventIndex() == GkashSoftPOSSDK.TransactionEventCallback.CANCEL_PAYMENT.ordinal()){
                                if(queryingStatus){
                                    Log.d(logEvent, "cancel query");
                                    queryingStatus = false;
                                    handler.removeCallbacks(runnable);
                                }
                                stopRunning();
                                mGkashSDKStatus.setTransactionEventCallback(GkashSoftPOSSDK.TransactionEventCallback.CANCEL_PAYMENT);
                            }
                        }else if (callback.getType() == GkashSoftPOSSDK.TransactionCallbackType.TRANSACTION_RESULT.ordinal()){
                            Log.d(logEvent, GkashSoftPOSSDK.TransactionCallbackType.TRANSACTION_RESULT.name());
                            if(queryingStatus){
                                Log.d(logEvent, "cancel query");
                                queryingStatus = false;
                                handler.removeCallbacks(runnable);
                            }

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

                            mSDK.SendTransactionResult(transactionDetails);
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
            if(queryingStatus){
                queryingStatus = false;
                handler.removeCallbacks(runnable);
                Log.d(logEvent, "queryingStatus false");
            }
        } catch (Exception e) {
            Log.d(logEvent, "Exception: " + e.getMessage());
            e.printStackTrace();
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

    public static String toJsonString(Object obj) {
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

    public void sendMessage(String jsonString) {
        // Add the message to the queue
        messageQueue.add(jsonString);
    }

    public void stopRunning() {
        Log.d(logEvent, "stopRunning");
        mGkashSDKStatus.setSocketStatus(GkashSoftPOSSDK.SocketConnectivityCallback.DISCONNECTED);
        running = false;
    }
}
