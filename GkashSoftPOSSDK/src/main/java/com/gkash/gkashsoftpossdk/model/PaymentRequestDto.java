package com.gkash.gkashsoftpossdk.model;

import com.gkash.gkashsoftpossdk.GkashSoftPOSSDK;

public class PaymentRequestDto {
    public String Amount;
    public String Email;
    public String MobileNo;
    public String ReferenceNo;
    public GkashSoftPOSSDK.PaymentType PaymentType;
    public boolean PreAuth;
    public String Signature;
}
