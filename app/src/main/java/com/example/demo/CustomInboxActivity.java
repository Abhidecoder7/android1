package com.example.demo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.CTInboxListener;
import com.clevertap.android.sdk.CTInboxStyleConfig;
import com.clevertap.android.sdk.inbox.CTInboxMessage;

import java.util.ArrayList;

public class CustomInboxActivity extends AppCompatActivity implements CTInboxListener {
    private RecyclerView recyclerView;
    private CustomInboxAdapter inboxAdapter;
    private CleverTapAPI clevertapInstance;
    private ProgressBar progressBar;
    private TextView emptyInboxText;
    private Button openInboxButton;
    private ArrayList<CTInboxMessage> inboxMessages = new ArrayList<>();
    private static final String TAG = "CustomInboxActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_inbox);

        // Initialize UI components
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyInboxText = findViewById(R.id.emptyInboxText);
        openInboxButton = findViewById(R.id.openInboxButton);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize CleverTap
        initializeCleverTap();

        // Handle Inbox Button Click
        openInboxButton.setOnClickListener(v -> showInbox());
    }

    private void initializeCleverTap() {
        clevertapInstance = CleverTapAPI.getDefaultInstance(getApplicationContext());

        if (clevertapInstance != null) {
            clevertapInstance.setCTNotificationInboxListener(this);
            clevertapInstance.initializeInbox();
        } else {
            Log.e(TAG, "CleverTap instance is null!");
            showToast("Error initializing CleverTap inbox");
        }
    }

    @Override
    public void inboxDidInitialize() {
        Log.d(TAG, "Inbox Initialized");
        fetchInboxMessages();
    }

    @Override
    public void inboxMessagesDidUpdate() {
        Log.d(TAG, "Inbox Messages Updated");
        fetchInboxMessages();
    }

    private void fetchInboxMessages() {
        if (clevertapInstance != null) {
            runOnUiThread(() -> {
                inboxMessages.clear();
                inboxMessages.addAll(clevertapInstance.getAllInboxMessages());

                progressBar.setVisibility(View.GONE);
                if (!inboxMessages.isEmpty()) {
                    inboxAdapter = new CustomInboxAdapter(this, inboxMessages, clevertapInstance);
                    recyclerView.setAdapter(inboxAdapter);
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyInboxText.setVisibility(View.GONE);
                } else {
                    recyclerView.setVisibility(View.GONE);
                    emptyInboxText.setVisibility(View.VISIBLE);
                }
            });
        } else {
            Log.e(TAG, "CleverTap instance is null!");
            showToast("Error loading inbox messages");
        }
    }

    private void showInbox() {
        if (clevertapInstance != null) {
            ArrayList<String> tabs = new ArrayList<>();
            tabs.add("Promotions");
            tabs.add("Offers"); // We support up to 2 tabs only.

            CTInboxStyleConfig styleConfig = new CTInboxStyleConfig();
            styleConfig.setFirstTabTitle("Inbox");
            styleConfig.setTabs(tabs);
            styleConfig.setTabBackgroundColor("#FF0000");
            styleConfig.setSelectedTabIndicatorColor("#0000FF");
            styleConfig.setSelectedTabColor("#0000FF");
            styleConfig.setUnselectedTabColor("#FFFFFF");
            styleConfig.setBackButtonColor("#FF0000");
            styleConfig.setNavBarTitleColor("#FF0000");
            styleConfig.setNavBarTitle("MY INBOX");
            styleConfig.setNavBarColor("#FFFFFF");
            styleConfig.setInboxBackgroundColor("#ADD8E6");

            clevertapInstance.showAppInbox(styleConfig);
        } else {
            showToast("CleverTap is not initialized");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (clevertapInstance != null) {
            clevertapInstance.initializeInbox();
        }
    }
}
