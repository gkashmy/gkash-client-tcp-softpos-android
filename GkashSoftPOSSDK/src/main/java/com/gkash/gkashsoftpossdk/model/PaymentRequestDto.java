package com.gkash.gkashsoftpossdk.model;

import com.gkash.gkashsoftpossdk.GkashSoftPOSSDK;

import org.jetbrains.annotations.NotNull;

public class PaymentRequestDto {
    private String Amount;
    private String Email;
    private String MobileNo;
    private String ReferenceNo;
    private GkashSoftPOSSDK.PaymentType PaymentType;
    private boolean PreAuth;
    private String Signature;

    public void setAmount(@NotNull String amount) {
        Amount = amount;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public void setMobileNo(String mobileNo) {
        MobileNo = mobileNo;
    }

    public void setReferenceNo(String referenceNo) {
        ReferenceNo = referenceNo;
    }

    public void setPaymentType(@NotNull GkashSoftPOSSDK.PaymentType paymentType) {
        PaymentType = paymentType;
    }

    public void setPreAuth(boolean preAuth) {
        PreAuth = preAuth;
    }

    public void setSignature(String signature) {
        Signature = signature;
    }

    public String getAmount() {
        return Amount;
    }

    public String getEmail() {
        return Email;
    }

    public String getMobileNo() {
        return MobileNo;
    }

    public String getReferenceNo() {
        return ReferenceNo;
    }

    public GkashSoftPOSSDK.PaymentType getPaymentType() {
        return PaymentType;
    }

    public boolean isPreAuth() {
        return PreAuth;
    }

    public String getSignature() {
        return Signature;
    }
}
