package com.karl.pfind.ui.services;

import com.google.firebase.auth.FirebaseUser;

public interface WebServiceCallback {

    void onWebServiceSuccess(String m,FirebaseUser fu);

    void onWebServiceError(String error);

}
