package com.karl.pfind.ui.services;


public interface WebServiceCallbackX {

    void onWebServiceSuccess(String m, Object res);

    void onWebServiceError(String error);
}