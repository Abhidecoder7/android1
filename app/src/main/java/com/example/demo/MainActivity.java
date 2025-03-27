package com.example.demo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.clevertap.android.pushtemplates.PushTemplateNotificationHandler;
import com.clevertap.android.sdk.CTInboxListener;
import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.displayunits.DisplayUnitListener;
import com.clevertap.android.sdk.displayunits.model.CleverTapDisplayUnit;
import com.clevertap.android.sdk.displayunits.model.CleverTapDisplayUnitContent;
import com.clevertap.android.sdk.interfaces.NotificationHandler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements CTInboxListener, DisplayUnitListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 2;

    private CleverTapAPI clevertapDefaultInstance;
    private EditText etName, etIdentity, etEmail, etContact;
    private Button btnCreate, btnUpdate, btnShowInApp, btnShowInbox;
    private ViewPager2 viewPager;
    private Handler autoScrollHandler;
    private Runnable autoScrollRunnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        initializeCleverTap();
        setupClickListeners();
        handleDeepLink(getIntent());
        requestPermissions();
    }

    private void initializeViews() {
        etName = findViewById(R.id.etName);
        etIdentity = findViewById(R.id.etIdentity);
        etEmail = findViewById(R.id.etEmail);
        etContact = findViewById(R.id.etContact);
        btnCreate = findViewById(R.id.btnCreate);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnShowInApp = findViewById(R.id.btnShowInApp);
        btnShowInbox = findViewById(R.id.btn_show_inbox);
        viewPager = findViewById(R.id.viewPager);
    }

    private void initializeCleverTap() {
        clevertapDefaultInstance = CleverTapAPI.getDefaultInstance(getApplicationContext());
        if (clevertapDefaultInstance != null) {
            CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.DEBUG);
            clevertapDefaultInstance.setOptOut(false);
            clevertapDefaultInstance.enableDeviceNetworkInfoReporting(true);
            clevertapDefaultInstance.setDisplayUnitListener(this);
            clevertapDefaultInstance.setCTNotificationInboxListener(this);
            clevertapDefaultInstance.initializeInbox();

            // Add Push Template Notification Handler
            CleverTapAPI.setNotificationHandler((NotificationHandler) new PushTemplateNotificationHandler());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        // Handle CleverTap push notification click
        CleverTapAPI clevertapInstance = CleverTapAPI.getDefaultInstance(this);
        if (clevertapInstance != null) {
            clevertapInstance.pushNotificationClickedEvent(intent.getExtras());
        }
    }

    private void setupClickListeners() {
        btnCreate.setOnClickListener(v -> createUserProfile());
        btnUpdate.setOnClickListener(v -> updateUserProfile());
        btnShowInApp.setOnClickListener(v -> triggerInAppNotification());
        btnShowInbox.setOnClickListener(v -> showAppInbox());
        Button second = findViewById(R.id.second_bt);
        second.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SecondActivity.class);
            startActivity(intent);
        });
        Button inboxButton = findViewById(R.id.inboxButton);
        inboxButton.setOnClickListener(v->{
            Intent customIntent = new Intent(MainActivity.this,CustomInboxActivity.class);
            startActivity(customIntent);
        });
        // Set click listener to open CustomInboxActivity
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission();
        }
        requestLocationPermission();
    }

    private void requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_NOTIFICATION_PERMISSION);
        }
    }

    private void createUserProfile() {
        if (clevertapDefaultInstance == null) return;

        String name = etName.getText().toString().trim();
        String identity = etIdentity.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etContact.getText().toString().trim();

        if (name.isEmpty() || identity.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            showToast("Please fill all fields");
            return;
        }

        HashMap<String, Object> profile = new HashMap<>();
        profile.put("Name", name);
        profile.put("Identity", identity);
        profile.put("Email", email);
        profile.put("Phone", phone);
        profile.put("Gender", "M");
        profile.put("DOB", Calendar.getInstance().getTime());

        clevertapDefaultInstance.onUserLogin(profile);
        showToast("User Profile Created!");
    }


    private void updateUserProfile() {
        if (clevertapDefaultInstance == null) return;

        String email = etEmail.getText().toString().trim();
        String phone = etContact.getText().toString().trim();

        if (email.isEmpty() || phone.isEmpty()) {
            showToast("Please fill email and phone fields");
            return;
        }

        HashMap<String, Object> profileUpdate = new HashMap<>();
        profileUpdate.put("Email", email);
        profileUpdate.put("Phone", phone);

        clevertapDefaultInstance.pushProfile(profileUpdate);
        showToast("User Profile Updated!");
    }

    private void triggerInAppNotification() {
        if (clevertapDefaultInstance != null) {
            clevertapDefaultInstance.pushEvent("Show In-App");
            showToast("In-App Notification Triggered!");
        }
    }



    private void showAppInbox() {
        if (clevertapDefaultInstance != null) {
            clevertapDefaultInstance.showAppInbox();
        }
    }

    @Override
    public void onDisplayUnitsLoaded(ArrayList<CleverTapDisplayUnit> displayUnits) {
        if (displayUnits == null || displayUnits.isEmpty()) return;

        for (CleverTapDisplayUnit unit : displayUnits) {
            List<String> imageUrls = new ArrayList<>();
            for (CleverTapDisplayUnitContent content : unit.getContents()) {
                if (content.getMedia() != null && !content.getMedia().isEmpty()) {
                    imageUrls.add(content.getMedia());
                }
            }

            if (!imageUrls.isEmpty()) {
                setupViewPager(imageUrls);
                clevertapDefaultInstance.pushDisplayUnitViewedEventForID(unit.getUnitID());
                break;
            }
        }
    }



    private void setupViewPager(List<String> imageUrls) {
        ImageCarouselAdapter adapter = new ImageCarouselAdapter(this, imageUrls);
        viewPager.setAdapter(adapter);
        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        setupAutoScroll(imageUrls.size());
    }

    private void setupAutoScroll(int itemCount) {
        if (autoScrollHandler != null && autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
        }

        autoScrollHandler = new Handler(Looper.getMainLooper());
        autoScrollRunnable = new Runnable() {
            int currentPage = 0;

            @Override
            public void run() {
                if (currentPage == itemCount) {
                    currentPage = 0;
                }
                viewPager.setCurrentItem(currentPage++, true);
                autoScrollHandler.postDelayed(this, 5000);
            }
        };
        autoScrollHandler.postDelayed(autoScrollRunnable, 5000);
    }

    @Override
    protected void onDestroy() {
        if (autoScrollHandler != null && autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
        }
        super.onDestroy();
    }

    private void handleDeepLink(Intent intent) {
        Uri data = intent.getData();
        if (data != null) {
            String deepLink = data.toString();
            Log.d(TAG, "Deep Link Clicked: " + deepLink);

            if (deepLink.contains("/app_inbox") && clevertapDefaultInstance != null) {
                clevertapDefaultInstance.showAppInbox();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setUserLocation();
        }
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            setUserLocation();
        }
    }

    private void setUserLocation() {
        if (clevertapDefaultInstance != null &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

            Location location = clevertapDefaultInstance.getLocation();
            if (location != null) {
                clevertapDefaultInstance.setLocation(location);
            } else {
                Log.w(TAG, "Location not available");
            }
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void inboxDidInitialize() {
        Log.d(TAG, "Inbox Initialized");
    }

    @Override
    public void inboxMessagesDidUpdate() {
        Log.d(TAG, "Inbox Messages Updated");
    }



}
