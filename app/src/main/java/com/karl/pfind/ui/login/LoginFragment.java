package com.karl.pfind.ui.login;

import static com.karl.pfind.Constants.GenericConstants.LOGIN_FRAGMENT_TAG;
import static com.karl.pfind.ui.login.LoginConstants.LOGIN_FAILED_MESSAGE;
import static com.karl.pfind.ui.login.LoginConstants.LOGIN_SUCCESS;

import android.content.Context;
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.karl.pfind.MapActivity;
import com.karl.pfind.R;
import com.karl.pfind.databinding.FragmentLoginBinding;
import com.karl.pfind.ui.register.RegisterFragment;

public class LoginFragment extends Fragment {

    private Context ctx;
    private FragmentLoginBinding binding;
    private LoginViewModel loginViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentLoginBinding.inflate(inflater, container, false);
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        ctx = getActivity().getApplicationContext();

        if(loginViewModel.getUserSession() != null) {
            redirectToMaps();
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        final EditText usernameEditText = binding.username;
        final EditText passwordEditText = binding.password;
        final Button loginButton = binding.login;
        final TextView signupButton = binding.signup;
        final ProgressBar loadingProgressBar = binding.loading;


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                loginViewModel
                        .login(
                            getActivity(),
                            usernameEditText.getText().toString(),
                            passwordEditText.getText().toString())
                        .observe(getActivity(), result -> {
                            if(result != null) {
                                if(result.getResultMessage().equals(LOGIN_SUCCESS)) {
                                    Toast.makeText(ctx,result.getResultMessage(),Toast.LENGTH_LONG).show();
                                    redirectToMaps();
                                } else {
                                    Toast.makeText(ctx,result.getResultMessage(),Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(ctx,result.getResultMessage(),Toast.LENGTH_LONG).show();
                            }
                        });
                loadingProgressBar.setVisibility(View.INVISIBLE);
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redirectToSignUpPage();
            }
        });
    }

    private void redirectToSignUpPage() {

        Fragment newFragment = new RegisterFragment();
        FragmentTransaction transaction = getActivity().
                                            getSupportFragmentManager()
                                            .beginTransaction();

        transaction.replace(R.id.main_activity_container_view, newFragment, LOGIN_FRAGMENT_TAG);
        transaction.addToBackStack(null);

        transaction.commit();

    }

    private void redirectToMaps() {
        Intent mapsActivity = new Intent(getActivity(), MapActivity.class);
        startActivity(mapsActivity);
    }

    /*private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
        if (getContext() != null && getContext().getApplicationContext() != null) {
            Toast.makeText(getContext().getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
        }
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        if (getContext() != null && getContext().getApplicationContext() != null) {
            Toast.makeText(
                    getContext().getApplicationContext(),
                    errorString,
                    Toast.LENGTH_LONG).show();
        }
    }
*/
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}