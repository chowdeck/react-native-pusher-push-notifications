
package com.b8ne.RNPusherPushNotifications;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

// SEE: https://docs.pusher.com/beams/reference/android

public class RNPusherPushNotificationsModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;
    private PusherWrapper pusher;
    private static Bundle pendingNotificationExtras = null;

    public RNPusherPushNotificationsModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    /**
     * Call this from MainActivity.onNewIntent() to capture notification data
     * when the app is brought from background by tapping a notification.
     */
    public static void onNewIntent(Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            boolean launchedFromHistory = (intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0;
            if (!launchedFromHistory) {
                pendingNotificationExtras = new Bundle(intent.getExtras());
            }
        }
    }

    private final LifecycleEventListener lifecycleEventListener = new LifecycleEventListener() {

        @Override
        public void onHostResume() {
            pusher.onResume(getCurrentActivity());
            emitPendingNotificationOpened();
        }

        @Override
        public void onHostDestroy() {
            pusher.onDestroy(getCurrentActivity());
        }

        @Override
        public void onHostPause() {
            pusher.onPause(getCurrentActivity());
        }
    };

    @Override
    public String getName() {
        return "RNPusherPushNotifications";
    }

    @ReactMethod
    public void setAppKey(String appKey) {
        this.pusher = new PusherWrapper(appKey, this.reactContext);
        reactContext.addLifecycleEventListener(lifecycleEventListener);
    }

    @ReactMethod
    public void clearAllState() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                pusher.clearAllState();
            }
        });
    }

    @ReactMethod
    public void subscribe(final String interest, final Callback errorCallback, final Callback successCallback) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                pusher.subscribe(interest, errorCallback, successCallback);
            }
        });
    }

    @ReactMethod
    public void unsubscribe(final String interest, final Callback errorCallback, final Callback successCallback) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                pusher.unsubscribe(interest, errorCallback, successCallback);
            }
        });
    }

    @ReactMethod
    public void unsubscribeAll(final Callback errorCallback, final Callback successCallback) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                pusher.unsubscribeAll(errorCallback, successCallback);
            }
        });
    }

    @ReactMethod
    public void getSubscriptions( final Callback subscriptionCallback, final Callback errorCallback) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                pusher.getSubscriptions(subscriptionCallback, errorCallback);
            }
        });
    }

    @ReactMethod
    public void setUserId(final String userId, final String token, final Callback errorCallback, final Callback successCallback) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                pusher.setUserId(userId, token, errorCallback, successCallback);
            }
        });
    }

    @ReactMethod
    public void setOnSubscriptionsChangedListener(final Callback subscriptionChangedListener) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                pusher.setOnSubscriptionsChangedListener(subscriptionChangedListener);
            }
        });
    }

    private void emitPendingNotificationOpened() {
        if (pendingNotificationExtras == null) {
            return;
        }

        Bundle extras = pendingNotificationExtras;
        pendingNotificationExtras = null;

        WritableMap payload = Arguments.createMap();
        for (String key : extras.keySet()) {
            Object value = extras.get(key);
            if (value != null) {
                payload.putString(key, value.toString());
            }
        }

        WritableMap result = Arguments.createMap();
        result.putMap("data", payload);

        reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit("notificationOpened", result);
    }

}
