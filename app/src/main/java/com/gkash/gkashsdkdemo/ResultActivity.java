package com.gkash.gkashsdkdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        TextView statusTV = findViewById(R.id.Status);
        TextView amountTV = findViewById(R.id.Amount);
        TextView applicationIdTV = findViewById(R.id.ApplicationId);
        TextView authIDResponseTV = findViewById(R.id.AuthIDResponse);
        TextView tvrTV = findViewById(R.id.TVR);
        TextView midTV = findViewById(R.id.MID);
        TextView tidTV = findViewById(R.id.TID);
        TextView cartIdTV = findViewById(R.id.CartId);
        TextView cidTV = findViewById(R.id.CompanyRemID);
        TextView cardNoTV = findViewById(R.id.CardNo);
        TextView cardTypeTV = findViewById(R.id.CardType);
        TextView traceNoTV = findViewById(R.id.TraceNo);
        TextView messageTV = findViewById(R.id.Message);
        TextView remIdTV = findViewById(R.id.RemID);
        TextView methodTV = findViewById(R.id.Method);
        TextView responseOrderNumberTV = findViewById(R.id.ResponseOrderNumber);
        TextView settlementBatchNumberTV = findViewById(R.id.SettlementBatchNumber);
        TextView transferDateTV = findViewById(R.id.TransferDate);
        TextView txTypeTV = findViewById(R.id.TxType);

        Button doneBtn = findViewById(R.id.doneBtn);

        doneBtn.setOnClickListener(view -> finish());

        Intent intent = getIntent();
        statusTV.setText(statusTV.getText() +  intent.getStringExtra("Status"));
        amountTV.setText(amountTV.getText() +  intent.getStringExtra("TransferCurrency") + intent.getStringExtra("TransferAmount"));
        applicationIdTV.setText(applicationIdTV.getText() +  intent.getStringExtra("ApplicationId"));
        authIDResponseTV.setText(authIDResponseTV.getText() +  intent.getStringExtra("AuthIDResponse"));
        tvrTV.setText(tvrTV.getText() +  intent.getStringExtra("TVR"));
        midTV.setText(midTV.getText() +  intent.getStringExtra("MID"));
        tidTV.setText(tidTV.getText() +  intent.getStringExtra("TID"));
        cartIdTV.setText(cartIdTV.getText() +  intent.getStringExtra("CartId"));
        cidTV.setText(cidTV.getText() +  intent.getStringExtra("CompanyRemID"));
        cardNoTV.setText(cardNoTV.getText() +  intent.getStringExtra("CardNo"));
        cardTypeTV.setText(cardTypeTV.getText() +  intent.getStringExtra("CardType"));
        traceNoTV.setText(traceNoTV.getText() +  intent.getStringExtra("TraceNo"));
        messageTV.setText(messageTV.getText() +  intent.getStringExtra("Message"));
        remIdTV.setText(remIdTV.getText() +  intent.getStringExtra("RemID"));
        methodTV.setText(methodTV.getText() +  intent.getStringExtra("Method"));
        responseOrderNumberTV.setText(responseOrderNumberTV.getText() +  intent.getStringExtra("ResponseOrderNumber"));
        settlementBatchNumberTV.setText(settlementBatchNumberTV.getText() +  intent.getStringExtra("SettlementBatchNumber"));
        transferDateTV.setText(transferDateTV.getText() +  intent.getStringExtra("TransferDate"));
        txTypeTV.setText(txTypeTV.getText() +  intent.getStringExtra("TxType"));
    }
}