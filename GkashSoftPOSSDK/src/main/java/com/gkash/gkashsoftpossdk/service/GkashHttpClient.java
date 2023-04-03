package com.gkash.gkashsoftpossdk.service;

import android.util.Log;

import com.gkash.gkashsoftpossdk.GkashSoftPOSSDK;
import com.gkash.gkashsoftpossdk.model.TransactionDetails;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.Scanner;

public class GkashHttpClient {
    private String HOST_URL = "";
    private final String logEvent = "GkashSDKClient";

    public GkashHttpClient(){
    }

    public void setHOST_URL(String HOST_URL) {
        this.HOST_URL = HOST_URL;
    }

    public String login(String jsonPayload){
        try {
            URL url = new URL(HOST_URL + "/apim/auth/userlogin");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            // add request header
            con.setRequestMethod("POST");
            Properties properties = System.getProperties();
            String userAgent =  properties.getProperty("http.agent");
            con.setRequestProperty("User-Agent", userAgent + "| GkashSDKVersion " + GkashSoftPOSSDK.VERSION);
            con.setRequestProperty("Content-Type", "application/json");
            // send post request
            con.setDoOutput(true);
            con.setDoInput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(jsonPayload);
            wr.flush();
            wr.close();

            // read response
            int responseCode = con.getResponseCode();
            Log.d(logEvent, "Response code: " + responseCode);
            if(responseCode == 200){
                InputStream inputStream = con.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                Log.d(logEvent, "Response body: " + response);
                JSONObject jsonObject = new JSONObject(response.toString());
                return jsonObject.getString("Auth");
            }else{
                return null;
            }

        } catch (Exception e) {
            Log.d(logEvent, "GkashHttpClient login:" + e.getMessage());
            return null;
        }
    }

    public String getSignatureKey(String token){
        try {
            URL url = new URL(HOST_URL + "/apim/merchant/signaturekey?authToken=" + token + "&sourceOfRequest=ANDROID");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            // add request header
            con.setRequestMethod("GET");
            Properties properties = System.getProperties();
            String userAgent =  properties.getProperty("http.agent");
            con.setRequestProperty("User-Agent", userAgent + "| GkashSDKVersion " + GkashSoftPOSSDK.VERSION);
            // read response
            int responseCode = con.getResponseCode();
            Log.d(logEvent, "Response code: " + responseCode);
            if(responseCode == 200){
                InputStream inputStream = con.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                Log.d(logEvent, "Response body: " + response);

                JSONObject jsonObject = new JSONObject(response.toString());
                JSONObject merchantObj = jsonObject.getJSONObject("Merchant");
                return merchantObj.getString("SignatureKey");
            }else{
                return null;
            }

        } catch (IOException | JSONException e) {
            Log.d(logEvent, "GkashHttpClient getSignatureKey:" + e.getMessage());
            return null;
        }
    }

    public String getIpAddress(String jsonPayload){
        try {
            URL url = new URL(HOST_URL + "/apim/auth/GetIpAddress");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            // add request header
            con.setRequestMethod("POST");
            Properties properties = System.getProperties();
            String userAgent =  properties.getProperty("http.agent");
            con.setRequestProperty("User-Agent", userAgent + "| GkashSDKVersion " + GkashSoftPOSSDK.VERSION);
            con.setRequestProperty("Content-Type", "application/json");
            // send post request
            con.setDoOutput(true);
            con.setDoInput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(jsonPayload);
            wr.flush();
            wr.close();

            // read response
            int responseCode = con.getResponseCode();
            Log.d(logEvent, "Response code: " + responseCode);

            if(responseCode == 200){
                InputStream inputStream = con.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                Log.d(logEvent, "Response body: " + response);

                JSONObject jsonObject = new JSONObject(response.toString());
                return jsonObject.getString("IpAddress");
            }else{
                return null;
            }

        } catch (IOException | JSONException e) {
            Log.d(logEvent, "GkashHttpClient getIpAddress:" + e.getMessage());
            return null;
        }
    }

    public TransactionDetails queryStatus(String remID, String token){
        try {
            URL url = new URL(HOST_URL + "/apim/transaction/detail?authToken=" + token + "&sourceOfRequest=ANDROID" + "&remID=" + remID);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            // add request header
            con.setRequestMethod("GET");
            Properties properties = System.getProperties();
            String userAgent =  properties.getProperty("http.agent");
            con.setRequestProperty("User-Agent", userAgent + "| GkashSDKVersion " + GkashSoftPOSSDK.VERSION);
            // read response
            int responseCode = con.getResponseCode();
            StringBuilder response = new StringBuilder();
            InputStream inputStream;
            if(responseCode == 200){
                inputStream = con.getInputStream();
            }else{
                inputStream = con.getErrorStream();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            Log.d(logEvent, "Response body: " + response);

            JSONObject jsonObject = new JSONObject(response.toString());
            if(responseCode == 200){
                JSONObject resultJsonObject = jsonObject.getJSONObject("Transaction");

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

                return transactionDetails;
            }else{
                String message = jsonObject.getString("Message");
                TransactionDetails transactionDetails = new TransactionDetails();
                transactionDetails.setMessage(message);

                return transactionDetails;
            }

        } catch (IOException | JSONException e) {
            Log.d(logEvent, "GkashHttpClient getSignatureKey:" + e.getMessage());
            return null;
        }
    }
}
