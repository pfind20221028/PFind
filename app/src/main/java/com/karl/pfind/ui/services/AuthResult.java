package com.karl.pfind.ui.services;

import com.google.firebase.auth.FirebaseUser;

public class AuthResult {

    private String resultMessage;
    private FirebaseUser user;

    public AuthResult(String resultMessage, FirebaseUser user) {
        this.resultMessage = resultMessage;
        this.user = user;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public FirebaseUser getUser() {
        return user;
    }

    public void setUser(FirebaseUser user) {
        this.user = user;
    }
}
