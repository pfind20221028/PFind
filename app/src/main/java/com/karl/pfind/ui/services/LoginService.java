package com.karl.pfind.ui.services;

import static com.karl.pfind.Constants.FirebaseConstants.DB_REF_USERS;
import static com.karl.pfind.Constants.FirebaseConstants.FIREBASE_INSTANCE;
import static com.karl.pfind.ui.login.LoginConstants.LOGIN_FAILED;
import static com.karl.pfind.ui.login.LoginConstants.LOGIN_SUCCESS;
import static com.karl.pfind.ui.login.LoginConstants.REGISTER_FAILED;
import static com.karl.pfind.ui.login.LoginConstants.REGISTER_SUCCESS;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.karl.pfind.Models.CustomLocation;
import com.karl.pfind.Models.Owner;

public class LoginService {

    private FirebaseDatabase database = FirebaseDatabase.getInstance(FIREBASE_INSTANCE);
    private DatabaseReference users = database.getReference(DB_REF_USERS);

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Deprecated
    public void login(WebServiceCallback callback,String username,String password) {

        Query query = users.orderByChild("username")
                        .equalTo(username)
                        .orderByChild("password")
                        .equalTo(password);

        query.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {

                    Log.e("firebase", "Error getting data", task.getException());
                    callback.onWebServiceError(task.getException().toString());
                }
                else {
                    if (isEmpty(task.getResult().getChildren())) {
                        callback.onWebServiceError(LOGIN_FAILED);
                    } else {
                        for (DataSnapshot childSnapshot: task.getResult().getChildren()) {
                            Owner o = childSnapshot.getValue(Owner.class);
                            Log.d("firebase", String.valueOf(childSnapshot.getKey()));
                            //callback.onWebServiceSuccess(o);
                        }
                    }
                }
            }
        });

    }

    @Deprecated
    private boolean isEmpty(Iterable<DataSnapshot> children) {
        int size = 0;
        for(DataSnapshot value : children) {
            size++;
        }
        return size == 0;
    }

    @Deprecated
    public void register(WebServiceCallback callback,String username,String password) {

        Query query = users.orderByChild("username")
                .equalTo(username);

        query.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {

                    Log.e("firebase", "Error getting data", task.getException());
                    callback.onWebServiceError(task.getException().toString());
                }
                else {
                    if (isEmpty(task.getResult().getChildren())) {
                        //callback.onWebServiceSuccess(createUser(username,password));
                    } else {
                        callback.onWebServiceError(REGISTER_FAILED);
                    }
                }
            }
        });
    }

    @Deprecated
    private Owner createUser(String username, String password) {
        Owner owner = new Owner();

        owner.setUsername(username);
        owner.setName("Test Name");
        owner.setEmail("test@email.com");
        owner.setPassword(password);
        owner.setPhoneNumber("1111111111");
        owner.setPetMissing(false);
        owner.setLastLocation(new CustomLocation(27.2046,77.4977));

        users.push().setValue(owner);

        return owner;
    }

    public void register(Activity ctx, WebServiceCallback callback, String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(ctx, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            callback.onWebServiceSuccess(REGISTER_SUCCESS,mAuth.getCurrentUser());
                        } else {
                            callback.onWebServiceError(REGISTER_FAILED + ": " + task.getException().toString());
                        }
                    }
                });
    }

    public void login(Activity ctx,WebServiceCallback callback,String email,String password) {

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(ctx, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            callback.onWebServiceSuccess(LOGIN_SUCCESS,mAuth.getCurrentUser());
                        } else {
                            callback.onWebServiceError(LOGIN_FAILED + ": " + task.getException().toString());
                        }
                    }
                });
    }

    public FirebaseUser isLoggedIn() {
        return mAuth.getCurrentUser();
    }

    public void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

}

