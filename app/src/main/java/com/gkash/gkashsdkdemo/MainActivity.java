package com.gkash.gkashsdkdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.gkash.gkashsoftpossdk.GkashSoftPOSSDK;
import com.gkash.gkashsoftpossdk.model.GkashSDKConfig;
import com.gkash.gkashsoftpossdk.model.PaymentRequestDto;
import com.gkash.gkashsoftpossdk.model.TransactionDetails;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private GkashSoftPOSSDK gkashSoftPOSSDK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();

        String username = intent.getStringExtra("username");
        String password = intent.getStringExtra("password");
        boolean testingEnv = intent.getBooleanExtra("env", true);

        Button requestPaymentBtn = findViewById(R.id.paymentBtn);
        Button tapToPhoneBtn = findViewById(R.id.tapBtn);
        Button cardBtn = findViewById(R.id.cardBtn);
        Button scanBtn = findViewById(R.id.scanBtn);
        Button duitNowBtn = findViewById(R.id.duitNowBtn);
        Button maybankBtn = findViewById(R.id.maybankBtn);
        Button boostBtn = findViewById(R.id.boostBtn);
        Button tngBtn = findViewById(R.id.tngBtn);
        Button grabPayBtn = findViewById(R.id.grabPayBtn);
        Button mcashBtn = findViewById(R.id.mcashBtn);
        Button shopeeBtn = findViewById(R.id.shopeeBtn);
        Button atomeBtn = findViewById(R.id.atomeBtn);
        Button aliPayBtn = findViewById(R.id.aliPayBtn);
        Button wechatBtn = findViewById(R.id.wechatBtn);
        Button queryBtn = findViewById(R.id.queryBtn);

        TextView socketStatusTV = findViewById(R.id.socketStatus);
        TextView transactionEventTV = findViewById(R.id.transactionEvent);
        TextView ipAddressTV = findViewById(R.id.ipAddressTV);

        EditText paymentTypeET = findViewById(R.id.paymentTypeET);
        EditText amountET = findViewById(R.id.amountET);
        EditText referenceNoET = findViewById(R.id.referenceNoET);

        CheckBox envCb = findViewById(R.id.envCB);
        envCb.setChecked(testingEnv);

        //Get Gkash sdk current instance
        gkashSoftPOSSDK = GkashSoftPOSSDK.getInstance(MainActivity.this);

        //Configure Config
        GkashSDKConfig gkashSDKConfig = new GkashSDKConfig().setUsername(username).setPassword(password).setTestingEnvironment(testingEnv);

        // To use the pfx file in your project, include the pfx file in the asset folder
        // GkashSDKConfig gkashSDKConfig = new GkashSDKConfig().setUsername(username).setPassword(password).setTestingEnvironment(testingEnv).setLoadCertFromAsset(true);

        // Do not need to call this if you setLoadCertFromAsset(true)
        // This function will navigate to your folder and choose the pfx file.
        gkashSoftPOSSDK.importGkashCert(MainActivity.this, 10001);
        //Initialize Gkash sdk
        gkashSoftPOSSDK.init(gkashSDKConfig, new GkashSoftPOSSDK.GkashStatusCallback() {
            @Override
            public void TransactionResult(TransactionDetails details) {
                //Handle transaction result
                Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
                intent.putExtra("Status", details.getStatus());
                intent.putExtra("TransferAmount", details.getTransferAmount());
                intent.putExtra("ApplicationId", details.getApplicationId());
                intent.putExtra("Message", details.getMessage());
                intent.putExtra("AuthIDResponse", details.getAuthIDResponse());
                intent.putExtra("CardNo", details.getCardNo());
                intent.putExtra("TraceNo", details.getTraceNo());
                intent.putExtra("MID", details.getMID());
                intent.putExtra("RemID", details.getRemID());
                intent.putExtra("TID", details.getTID());
                intent.putExtra("TVR", details.getTVR());
                intent.putExtra("ResponseOrderNumber", details.getResponseOrderNumber());
                intent.putExtra("Method", details.getMethod());
                intent.putExtra("TransferDate", details.getTransferDate());
                intent.putExtra("TransferCurrency", details.getTransferCurrency());
                intent.putExtra("SettlementBatchNumber", details.getSettlementBatchNumber());
                intent.putExtra("CardType", details.getCardType());
                intent.putExtra("TxType", details.getTxType());
                intent.putExtra("CartId", details.getCartID());
                startActivity(intent);
            }

            @Override
            public void SocketStatus(GkashSoftPOSSDK.SocketConnectivityCallback connectivityCallback) {
                runOnUiThread(() -> {
                    socketStatusTV.setText(connectivityCallback.name());

                    if(connectivityCallback == GkashSoftPOSSDK.SocketConnectivityCallback.ONLINE){
                        ipAddressTV.setText("IP address: " + gkashSoftPOSSDK.getIpAddress());
                    }
                });
            }

            @Override
            public void TransactionEvent(GkashSoftPOSSDK.TransactionEventCallback transactionEventCallback) {
                runOnUiThread(() -> transactionEventTV.setText(transactionEventCallback.name()));
            }

            @Override
            public void QueryTransactionResult(TransactionDetails details) {
                //Handle transaction result
                Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
                intent.putExtra("Status", details.getStatus());
                intent.putExtra("TransferAmount", details.getTransferAmount());
                intent.putExtra("ApplicationId", details.getApplicationId());
                intent.putExtra("Message", details.getMessage());
                intent.putExtra("AuthIDResponse", details.getAuthIDResponse());
                intent.putExtra("CardNo", details.getCardNo());
                intent.putExtra("TraceNo", details.getTraceNo());
                intent.putExtra("MID", details.getMID());
                intent.putExtra("RemID", details.getRemID());
                intent.putExtra("TID", details.getTID());
                intent.putExtra("TVR", details.getTVR());
                intent.putExtra("ResponseOrderNumber", details.getResponseOrderNumber());
                intent.putExtra("Method", details.getMethod());
                intent.putExtra("TransferDate", details.getTransferDate());
                intent.putExtra("TransferCurrency", details.getTransferCurrency());
                intent.putExtra("SettlementBatchNumber", details.getSettlementBatchNumber());
                intent.putExtra("CardType", details.getCardType());
                intent.putExtra("TxType", details.getTxType());
                intent.putExtra("CartId", details.getCartID());
                startActivity(intent);
            }
        });

        //send payment request to softpos
        requestPaymentBtn.setOnClickListener(view -> {
            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
            String formattedDate = df.format(c);

            referenceNoET.setText(formattedDate);

            String paymentType = paymentTypeET.getText().toString();
            PaymentRequestDto requestDto = new PaymentRequestDto();
            requestDto.setAmount(amountET.getText().toString());
            requestDto.setPaymentType(GkashSoftPOSSDK.PaymentType.valueOf(paymentType));
            requestDto.setReferenceNo(formattedDate);
            requestDto.setPreAuth(false);
            requestDto.setEmail("YourEmail@email.com");
            requestDto.setMobileNo("0123456789");

            queryBtn.setEnabled(true);
            gkashSoftPOSSDK.requestPayment(requestDto);
        });

        queryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String referenceNo = referenceNoET.getText().toString();

                if(!referenceNo.isEmpty()){
                    gkashSoftPOSSDK.queryTransactionStatus(referenceNo);
                }
            }
        });

        //Payment type button
        tapToPhoneBtn.setOnClickListener(view -> paymentTypeET.setText(GkashSoftPOSSDK.PaymentType.TAPTOPHONE_PAYMENT.name()));
        cardBtn.setOnClickListener(view -> paymentTypeET.setText(GkashSoftPOSSDK.PaymentType.CARD_PAYMENT.name()));
        scanBtn.setOnClickListener(view -> paymentTypeET.setText(GkashSoftPOSSDK.PaymentType.EWALLET_SCAN_PAYMENT.name()));
        duitNowBtn.setOnClickListener(view -> paymentTypeET.setText(GkashSoftPOSSDK.PaymentType.DUITNOW_QR.name()));
        maybankBtn.setOnClickListener(view -> paymentTypeET.setText(GkashSoftPOSSDK.PaymentType.MAYBANK_QR.name()));
        boostBtn.setOnClickListener(view -> paymentTypeET.setText(GkashSoftPOSSDK.PaymentType.BOOST_QR.name()));
        tngBtn.setOnClickListener(view -> paymentTypeET.setText(GkashSoftPOSSDK.PaymentType.TNG_QR.name()));
        grabPayBtn.setOnClickListener(view -> paymentTypeET.setText(GkashSoftPOSSDK.PaymentType.GRABPAY_QR.name()));
        mcashBtn.setOnClickListener(view -> paymentTypeET.setText(GkashSoftPOSSDK.PaymentType.MCASH_QR.name()));
        shopeeBtn.setOnClickListener(view -> paymentTypeET.setText(GkashSoftPOSSDK.PaymentType.SHOPEE_PAY_QR.name()));
        atomeBtn.setOnClickListener(view -> paymentTypeET.setText(GkashSoftPOSSDK.PaymentType.ATOME_QR.name()));
        aliPayBtn.setOnClickListener(view -> paymentTypeET.setText(GkashSoftPOSSDK.PaymentType.ALIPAY_QR.name()));
        wechatBtn.setOnClickListener(view -> paymentTypeET.setText(GkashSoftPOSSDK.PaymentType.WECHAT_QR.name()));
    }

    // Do not need to add this function if you set this config -> .setLoadCertFromAsset(true);
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == 10001 && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                Uri uri = resultData.getData();
                gkashSoftPOSSDK.setGkashCertUri(uri);
            }
        }
    }
}