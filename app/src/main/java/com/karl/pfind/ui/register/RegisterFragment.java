package com.karl.pfind.ui.register;

import static com.karl.pfind.Constants.GenericConstants.LOGIN_FRAGMENT_TAG;
import static com.karl.pfind.ui.login.LoginConstants.LOGIN_FAILED_MESSAGE;
import static com.karl.pfind.ui.login.LoginConstants.REGISTER_EMAIL_EXIST;
import static com.karl.pfind.ui.login.LoginConstants.REGISTER_SUCCESS;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.karl.pfind.MapActivity;
import com.karl.pfind.R;
import com.karl.pfind.bluno.BlunoLibrary;
import com.karl.pfind.bluno.BlunoLibraryFragment;
import com.karl.pfind.databinding.FragmentLoginBinding;
import com.karl.pfind.databinding.FragmentRegisterBinding;
import com.karl.pfind.ui.login.LoginFragment;
import com.karl.pfind.ui.login.LoginViewModel;

import java.util.List;

public class RegisterFragment extends Fragment {

    private Context ctx;
    private FragmentRegisterBinding binding;
    private LoginViewModel loginViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        ctx = getActivity();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final EditText usernameEditText = binding.etxUsername;
        final EditText passwordEditText = binding.etxPassword;
        final EditText passwordConfirmEditText = binding.etxPasswordConfirm;
        final Button registerButton = binding.register;
        final TextView txv_login = binding.txvLogin;
        final ProgressBar loadingProgressBar = binding.loading;


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (usernameEditText.getText().toString().isEmpty() ||
                        passwordEditText.getText().toString().isEmpty() ||
                        passwordConfirmEditText.getText().toString().isEmpty()) {
                    Toast.makeText(ctx,"Error! Please enter required fields.",Toast.LENGTH_LONG).show();
                    return;
                }

                if(!passwordEditText.getText().toString().equals(passwordConfirmEditText.getText().toString())) {
                    Toast.makeText(ctx,"Error! Passwords should match.",Toast.LENGTH_LONG).show();
                    return;
                }

                loadingProgressBar.setVisibility(View.VISIBLE);
                loginViewModel
                        .register(
                                getActivity(),
                                usernameEditText.getText().toString(),
                                passwordEditText.getText().toString())
                        .observe(getActivity(), result -> {
                            if(result != null) {
                                Toast.makeText(ctx,result.getResultMessage(),Toast.LENGTH_LONG).show();

                                if(!result.getResultMessage().equals("Register Failed: com.google.firebase.auth.FirebaseAuthWeakPasswordException: The given password is invalid. [ Password should be at least 6 characters ]")
                                    && !result.getResultMessage().equals("Register Failed: com.google.firebase.auth.FirebaseAuthUserCollisionException: The email address is already in use by another account."))
                                    confirmDeviceOwnerSetup();
                            } else {
                                Toast.makeText(ctx,result.getResultMessage(),Toast.LENGTH_LONG).show();
                            }
                        });
                loadingProgressBar.setVisibility(View.INVISIBLE);
            }
        });

        txv_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redirectToLoginPage();
            }
        });

    }

    private void confirmDeviceOwnerSetup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder
                .setMessage("Do you want to setup your device?")
                .setTitle("Device setup")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //
                        //redirectToRegisterDevicePage();
                        redirectToMaps();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        redirectToMaps();
                    }
                });
        builder.setCancelable(false);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void redirectToMaps() {
        Intent mapsActivity = new Intent(getActivity(), MapActivity.class);
        startActivity(mapsActivity);

        removeThisFragment();
    }

    private void removeThisFragment() {

        Fragment newFragment = new LoginFragment();
        FragmentTransaction transaction = getActivity().
                getSupportFragmentManager()
                .beginTransaction();

        transaction.replace(R.id.main_activity_container_view, newFragment, LOGIN_FRAGMENT_TAG);
        transaction.addToBackStack(null);

        transaction.commit();

        /*Fragment fragment = getActivity().
                getSupportFragmentManager().findFragmentByTag(LOGIN_FRAGMENT_TAG);
        if(fragment != null)
            getActivity().
                    getSupportFragmentManager().beginTransaction().remove(fragment).commit();*/
    }

    private void redirectToLoginPage() {
        Fragment newFragment = new LoginFragment();
        FragmentTransaction transaction = getActivity().
                getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.main_activity_container_view, newFragment);
        transaction.addToBackStack(null);

        transaction.commit();
    }

    private void redirectToRegisterDevicePage() {
        Fragment newFragment = new RegisterDeviceFragment();
        FragmentTransaction transaction = getActivity().
                getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.main_activity_container_view, newFragment);
        transaction.addToBackStack(null);

        transaction.commit();
    }


}
