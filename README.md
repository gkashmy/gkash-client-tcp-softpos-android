# gkash-client-tcp-softpos-android (Offline payment)

This project serves as a sample that demonstrates how to communicate with the Gkash SoftPOS APP and perform offline payments.

## Usage
Ensure jcenter is in the root level build.gradle file of an Android Project.

```Gradle
repositories {
    mavenCentral()
}
```


Include the library reference in the build.gradle file.

```Gradle
dependencies {
     implementation 'io.github.gkashmy:gkash-softpos-sdk:2.1.1'
}
```
Implement the library as follows. 

SDK Initialization
| GkashSDKConfig  | Description | Type |
| --- | --- | --- |
| setUsername | SoftPOS login username | string |
| setPassword | SoftPOS login password | string |
| setTestingEnvironment | Pass true to enable production environment, pass false for staging environment | boolean |
| setLoadCertFromAsset | To use the gkash pfx file from your project asset folder | boolean |

```Java
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
```
After called importGkashCert function and choose the pfx file from your folder, implement onActivityResult to get the URI of the pfx file.
Then call setGkashCertUri(uri) function from gkash sdk to import the pfx file.
Note: You do not need to add this function when you set .setLoadCertFromAsset(true) to GkashSDKConfig;
```Java
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
```

## Setup Gkash Business settings

Make sure to turn on Server mode in Gkash Business settings before initializing the SDK.

![ss](https://github.com/gkashmy/gkash-client-tcp-softpos-android/assets/72077476/c7b38acd-3bff-4efe-b9b8-34fb8fac1c89)

## License
[Apache 2.0](https://choosealicense.com/licenses/apache-2.0/)
