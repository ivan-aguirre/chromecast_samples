package org.gcastsamples.castnotifications;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.google.sample.castcompanionlibrary.cast.exceptions.NoConnectionException;
import com.google.sample.castcompanionlibrary.cast.exceptions.TransientNetworkDisconnectionException;

public class MyNotificationListenerService extends NotificationListenerService {

    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
        try {
            MyApplication.getVideoCastManager(getApplicationContext()).sendDataMessage(
                        String.valueOf(statusBarNotification.getNotification().tickerText));
        } catch (TransientNetworkDisconnectionException e) {
            Log.e("NotificationListenerService", "Can't send message",  e);
        } catch (NoConnectionException e) {
            Log.e("NotificationListenerService", "Can't send message",  e);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification statusBarNotification) {

    }
}
