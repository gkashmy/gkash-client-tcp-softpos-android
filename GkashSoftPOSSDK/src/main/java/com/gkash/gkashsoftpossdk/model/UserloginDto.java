package com.gkash.gkashsoftpossdk.model;

public class UserloginDto {
    private String Username;
    private String password;
    private final String CompanyRemID = "ANDROID";

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
