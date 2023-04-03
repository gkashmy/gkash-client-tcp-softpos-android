package com.gkash.gkashsoftpossdk.model;

public class GkashSDKConfig {
    private String Username;
    private String Password;
    private String CertPath;
    private boolean TestingEnvironment;

    public String getUsername() {
        return Username;
    }

    public GkashSDKConfig setUsername(String username) {
        Username = username;
        return this;
    }

    public String getPassword() {
        return Password;
    }

    public GkashSDKConfig setPassword(String password) {
        Password = password;
        return this;
    }

    public boolean isTestingEnvironment() {
        return TestingEnvironment;
    }

    public GkashSDKConfig setTestingEnvironment(boolean testingEnvironment) {
        TestingEnvironment = testingEnvironment;
        return this;
    }

    public String getCertPath() {
        return CertPath;
    }

    public GkashSDKConfig setCertPath(String certPath) {
        CertPath = certPath;
        return this;
    }
}
