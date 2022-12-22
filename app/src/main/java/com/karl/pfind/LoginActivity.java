package com.karl.pfind;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.karl.pfind.R;
import com.karl.pfind.ui.login.LoginFragment;
import com.karl.pfind.ui.register.RegisterDeviceFragment;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.main_activity_container_view, LoginFragment.class, null)
                    //.add(R.id.main_activity_container_view, RegisterDeviceFragment.class, null)
                    .commit();
        }

        //Debug Mode
        /*Intent mapsActivity = new Intent(this, MapActivity.class);
        startActivity(mapsActivity);*/
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}