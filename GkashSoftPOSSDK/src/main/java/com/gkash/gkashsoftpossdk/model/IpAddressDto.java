package com.gkash.gkashsoftpossdk.model;

public class IpAddressDto {
    private String RemID;
    private String AuthToken;
    private final String CompanyRemID = "ANDROID";

    public String getRemID() {
        return RemID;
    }

    public void setRemID(String remID) {
        RemID = remID;
    }

    public String getAuthToken() {
        return AuthToken;
    }

    public void setAuthToken(String authToken) {
        AuthToken = authToken;
    }
}
