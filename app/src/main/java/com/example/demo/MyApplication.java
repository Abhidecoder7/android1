package com.example.demo;

import android.app.Application;
import com.clevertap.android.sdk.ActivityLifecycleCallback;
import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.interfaces.NotificationHandler;
import com.clevertap.android.pushtemplates.PushTemplateNotificationHandler;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize CleverTap
        ActivityLifecycleCallback.register(this);

        // Set the notification handler for push templates
        CleverTapAPI.setNotificationHandler((NotificationHandler)new PushTemplateNotificationHandler());

        // Debug level is already set in MainActivity, so no need to set it here
    }
}