package com.example.demo;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.pushnotification.fcm.CTFcmMessageHandler;

public class MyFcmMessageListenerService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        CleverTapAPI clevertapDefaultInstance = CleverTapAPI.getDefaultInstance(getApplicationContext());
        clevertapDefaultInstance.pushFcmRegistrationId(token, true);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        new CTFcmMessageHandler().createNotification(getApplicationContext(), remoteMessage);
    }
}
