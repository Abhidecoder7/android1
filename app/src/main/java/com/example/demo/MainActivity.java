package com.example.demo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.clevertap.android.sdk.CTInboxListener;
import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.displayunits.DisplayUnitListener;
import com.clevertap.android.sdk.displayunits.model.CleverTapDisplayUnit;
import com.clevertap.android.sdk.displayunits.model.CleverTapDisplayUnitContent;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements CTInboxListener, DisplayUnitListener {

    private CleverTapAPI cleverTap;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1;
    private LinearLayout nativeDisplayContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cleverTap = CleverTapAPI.getDefaultInstance(this);
        CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.DEBUG);

        if (cleverTap != null) {
            cleverTap.setOptOut(false);
            cleverTap.enableDeviceNetworkInfoReporting(true);
//            cleverTap.setDisplayUnitListener(this);
//            cleverTap.setCTNotificationInboxListener(this);
//            cleverTap.initializeInbox();
        }

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION);
            }
        }

        // UI elements
        Button btnCreate = findViewById(R.id.btnCreate);
        Button btnUpdate = findViewById(R.id.btnUpdate);
        Button btnShowInApp = findViewById(R.id.btnShowInApp);
        Button btnShowInbox = findViewById(R.id.btn_show_inbox);
        Button btnShowNativeDisplay = findViewById(R.id.btn_fetch_native_display);
        nativeDisplayContainer = findViewById(R.id.native_display_container);

        btnCreate.setOnClickListener(v -> createUserProfile());
        btnUpdate.setOnClickListener(v -> updateUserProfile());
        btnShowInApp.setOnClickListener(v -> triggerInAppNotification());
        btnShowInbox.setOnClickListener(v -> showAppInbox());
        btnShowNativeDisplay.setOnClickListener(v -> fetchNativeDisplay());
    }

    private void createUserProfile() {
        if (cleverTap != null) {
            HashMap<String, Object> profile = new HashMap<>();
            profile.put("Name", "Abh");
            profile.put("Identity", "100");
            profile.put("Email", "abhiiiii@example.com");
            profile.put("Phone", "+91234578767");
            profile.put("Gender", "M");
            profile.put("DOB", new Date());
            cleverTap.onUserLogin(profile);
            showToast("User Profile Created!");
        }
    }

    private void updateUserProfile() {
        if (cleverTap != null) {
            HashMap<String, Object> profileUpdate = new HashMap<>();
            profileUpdate.put("Phone", "+91 0000000");
            profileUpdate.put("Photo", "https://example.com/new-photo.jpg");
            profileUpdate.put("Location", "Mumbai, India");
            cleverTap.pushProfile(profileUpdate);
            showToast("User Profile Updated!");
        }
    }

    private void triggerInAppNotification() {
        if (cleverTap != null) {
            cleverTap.pushEvent("Show In-App");
            showToast("In-App Notification Triggered!");
        }
    }

    private void showAppInbox() {
        if (cleverTap != null) {
            cleverTap.showAppInbox();
            showToast("Opening App Inbox...");
        }
    }

    private void fetchNativeDisplay() {
        if (cleverTap != null) {
            cleverTap.pushEvent("Show_Native_Display");
            ArrayList<CleverTapDisplayUnit> units = cleverTap.getAllDisplayUnits();
            if (units != null && !units.isEmpty()) {
                processDisplayUnits(units);
            }
        }
    }

    private void processDisplayUnits(ArrayList<CleverTapDisplayUnit> units) {
        nativeDisplayContainer.removeAllViews();
        for (CleverTapDisplayUnit unit : units) {
            prepareDisplayView(unit);
            cleverTap.pushDisplayUnitViewedEventForID(unit.getUnitID());
        }
    }

    private void prepareDisplayView(CleverTapDisplayUnit unit) {
        for (CleverTapDisplayUnitContent content : unit.getContents()) {
            TextView title = new TextView(this);
            title.setText(content.getTitle());
            title.setTextSize(18);
            title.setTextColor(Color.BLACK);
            nativeDisplayContainer.addView(title);

            TextView message = new TextView(this);
            message.setText(content.getMessage());
            message.setTextSize(14);
            nativeDisplayContainer.addView(message);

            if (content.getMedia() != null && !content.getMedia().isEmpty()) {
                ImageView imageView = new ImageView(this);
                Glide.with(this).load(content.getMedia()).into(imageView);
                nativeDisplayContainer.addView(imageView);
            }
        }

        nativeDisplayContainer.setOnClickListener(v -> {
            cleverTap.pushDisplayUnitClickedEventForID(unit.getUnitID());
            String url = unit.getCustomExtras().get("url");
            if (url != null) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
        });
    }

    @Override
    public void onDisplayUnitsLoaded(ArrayList<CleverTapDisplayUnit> units) {
        if (units != null && !units.isEmpty()) {
            processDisplayUnits(units);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void inboxDidInitialize() {
        showToast("Inbox Initialized");
    }

    @Override
    public void inboxMessagesDidUpdate() {
        showToast("Inbox Messages Updated");
    }
}
