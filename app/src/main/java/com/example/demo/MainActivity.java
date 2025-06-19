package com.example.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.clevertap.android.sdk.CleverTapAPI;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    EditText nameInput, emailInput;
    Button signupButton;
    CleverTapAPI cleverTapAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.VERBOSE); // for debugging

        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        signupButton = findViewById(R.id.signupButton);

        signupButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Please enter name and email", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Get default instance (internally initialized by CleverTap SDK)
            cleverTapAPI = CleverTapAPI.getDefaultInstance(getApplicationContext());
            if (cleverTapAPI == null) {
                Log.e("CT", "CleverTap instance is null");
                Toast.makeText(this, "CleverTap not initialized properly", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Set user profile
            HashMap<String, Object> profile = new HashMap<>();
            profile.put("Name", name);
            profile.put("Email", email);
            profile.put("Identity", email); // must be unique

            cleverTapAPI.onUserLogin(profile); // 🔁 login + create profile
            cleverTapAPI.pushEvent("User Signed Up");

            Toast.makeText(this, "User profile created on CleverTap", Toast.LENGTH_SHORT).show();
            Log.d("CT", "Profile created: " + profile.toString());
        });
    }
}
