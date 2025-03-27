package com.example.demo;

import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.pushnotification.NotificationInfo;
import com.clevertap.android.sdk.pushnotification.fcm.CTFcmMessageHandler;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import android.os.Bundle;
import android.util.Log;

import java.util.Map;

public class MyFcmMessageListenerService extends FirebaseMessagingService {

    private static final String TAG = "MyFcmService";

    @Override
    public void onMessageReceived(RemoteMessage message) {
        try {
            if (message.getData().size() > 0) {
                // Let CleverTap's push handler process the message
                new CTFcmMessageHandler().createNotification(getApplicationContext(), message);

                // Extract notification information
                //convert the json data into bundle
                Bundle extras = new Bundle();
                for (Map.Entry<String, String> entry : message.getData().entrySet()) {
                    extras.putString(entry.getKey(), entry.getValue());
                }

                NotificationInfo info = CleverTapAPI.getNotificationInfo(extras);

                if (!info.fromCleverTap) {
                    Log.d(TAG, "Non-CleverTap notification received, handle separately.");
                    // Handle non-CleverTap push notification if necessary
                }
            }
        } catch (Throwable t) {
            Log.e(TAG, "Error parsing FCM message", t);
        }
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed FCM token: " + token);

        // Forward FCM registration token to CleverTap
        CleverTapAPI clevertapDefaultInstance = CleverTapAPI.getDefaultInstance(getApplicationContext());
        if (clevertapDefaultInstance != null) {
            clevertapDefaultInstance.pushFcmRegistrationId(token, true);
        }
    }
}
