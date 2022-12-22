package com.karl.pfind.ui.login;

import static com.karl.pfind.ui.login.LoginConstants.LOGIN_FAILED;
import static com.karl.pfind.ui.login.LoginConstants.REGISTER_FAILED;

import android.app.Activity;
import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.karl.pfind.Models.PFindDevice;
import com.karl.pfind.ui.services.AuthResult;
import com.karl.pfind.ui.services.LoginService;
import com.karl.pfind.ui.services.RegisterDeviceService;
import com.karl.pfind.ui.services.Result;
import com.karl.pfind.ui.services.WebServiceCallback;
import com.karl.pfind.ui.services.WebServiceCallbackX;

public class LoginViewModel extends ViewModel implements WebServiceCallback {

    private LoginService loginService =  new LoginService();
    private RegisterDeviceService registerDeviceService =  new RegisterDeviceService();

    private MutableLiveData<AuthResult> authResult;
    private MutableLiveData<Result> result;

    @Deprecated
    public LiveData<AuthResult> login(String un,String pw) {
        if (authResult == null) {
            authResult = new MutableLiveData<>();
            findByOwnerAndPassword(un,pw);
        }
        return authResult;
    }

    @Deprecated
    private void findByOwnerAndPassword(String un,String pw) {
        // Do an asynchronous operation to fetch users.
        loginService.login(this,un,pw);
    }

    public LiveData<AuthResult> register(Activity context, String un, String pw) {
        if (authResult == null)
            authResult = new MutableLiveData<>();
        loginService.register(context,this,un,pw);
        return authResult;
    }

    public LiveData<AuthResult> login(Activity context,String un,String pw) {
        if (authResult == null)
            authResult = new MutableLiveData<>();
        loginService.login(context,this,un,pw);
        return authResult;
    }

    public FirebaseUser getUserSession() {
        return loginService.isLoggedIn();
    }

    public void signOut() {
        loginService.signOut();
    }

    @Override
    public void onWebServiceSuccess(String message,FirebaseUser u) {
        authResult.setValue(new AuthResult(message,u));
    }

    @Override
    public void onWebServiceError(String error) {
        if(error.contains(LOGIN_FAILED) || error.contains(REGISTER_FAILED))
            authResult.setValue(new AuthResult(error,null));
    }

    public LiveData<Result> registerDevice(String name, String macAd) {
        if (result == null)
            result = new MutableLiveData<>();
        registerDeviceService.registerDevice(
                name,
                macAd,
                new WebServiceCallbackX() {
                    @Override
                    public void onWebServiceSuccess(String m, Object res) {
                        result.setValue(new Result(m,res));
                    }

                    @Override
                    public void onWebServiceError(String error) {
                        result.setValue(new Result(error,null));
                    }
                });
        return result;
    }

    public LiveData<Result> findDeviceByUser() {
        if (result == null)
            result = new MutableLiveData<>();
        registerDeviceService.retrieveUserDevice(
                new WebServiceCallbackX() {
                    @Override
                    public void onWebServiceSuccess(String m, Object res) {
                        result.setValue(new Result(m,res));
                    }

                    @Override
                    public void onWebServiceError(String error) {
                        result.setValue(new Result(error,null));
                    }
                });
        return result;
    }
}


