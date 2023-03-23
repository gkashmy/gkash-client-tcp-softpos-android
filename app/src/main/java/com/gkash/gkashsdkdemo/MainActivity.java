package com.gkash.gkashsdkdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.TextView;

import com.gkash.gkashsoftpossdk.GkashSoftPOSSDK;
import com.gkash.gkashsoftpossdk.model.PaymentRequestDto;
import com.gkash.gkashsoftpossdk.model.TransactionDetails;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Environment.getDataDirectory();
        Button button = findViewById(R.id.paymentBtn);
        TextView socketStatusTV = findViewById(R.id.socketStatus);
        TextView transactionEventTV = findViewById(R.id.transactionEvent);

        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String formattedDate = df.format(c);

        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.Amount = "1.00";
        requestDto.PaymentType = GkashSoftPOSSDK.PaymentType.TAPTOPHONE_PAYMENT;
        requestDto.ReferenceNo = formattedDate;
        requestDto.PreAuth = false;
        requestDto.Email = "";
        requestDto.MobileNo = "";
        final GkashSoftPOSSDK gkashSoftPOSSDK = GkashSoftPOSSDK.getInstance(getApplication());

        button.setOnClickListener(view -> gkashSoftPOSSDK.requestPayment(requestDto, new GkashSoftPOSSDK.GkashStatusCallback() {
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
                runOnUiThread(() -> socketStatusTV.setText(connectivityCallback.name()));
            }

            @Override
            public void TransactionEvent(GkashSoftPOSSDK.TransactionEventCallback transactionEventCallback) {
                runOnUiThread(() -> transactionEventTV.setText(transactionEventCallback.name()));
            }
        }));
    }
}