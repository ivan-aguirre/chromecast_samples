package com.dropboxcast.dropboxcast;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.io.IOException;

public class CastController implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<Cast.ApplicationConnectionResult>,
        RemoteMediaPlayer.OnStatusUpdatedListener,
        RemoteMediaPlayer.OnMetadataUpdatedListener{

    private static final String TAG = CastController.class.getSimpleName();

    private static final String RECEIVER_ID = CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID;
    
    private MediaRouter mediaRouter;
    private MediaRouteSelector mediaRouteSelector;
    private MediaRouterCallback mediaRouterCallback;

    private CastDevice selectedDevice;
    private GoogleApiClient apiClient;
    private RemoteMediaPlayer mRemoteMediaPlayer;

    private boolean waitingForReconnect;
    private boolean controllerStarted;
    private boolean applicationStarted;

    private String sessionId;
    private String routeId;
    private MediaRouter.RouteInfo routeToReconnect;

    private Context ctx;

    private SharedPreferences preferences;

    private SendingContentCallback sendingContentCallback;

    public static interface SendingContentCallback {
        void onStartSending();
        void onContentSent(boolean success);
    }

    public CastController(SendingContentCallback sendingContentCallback) {
        if (sendingContentCallback == null) {
            throw new NullPointerException("sendingContentCallback");
        }
        this.sendingContentCallback = sendingContentCallback;
    }

    public void startOrReconnect(Context ctx) {
        if (ctx == null) {
            throw new NullPointerException("Context");
        }

        if (this.ctx == null) {
            this.ctx = ctx.getApplicationContext();
            this.preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        }

        initializeMediaRouterSelector();

        if (!controllerStarted) {
            reconnectToDeviceIfPossible();
            this.controllerStarted = true;
        }
    }

    private void initializeMediaRouterSelector() {
        this.mediaRouter = MediaRouter.getInstance(this.ctx);

        String categoryForCast = CastMediaControlIntent.categoryForCast(RECEIVER_ID);

        this.mediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(categoryForCast)
                .build();

        this.mediaRouterCallback = new MediaRouterCallback();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (waitingForReconnect) {
            waitingForReconnect = false;
            reconnectChannels();
        } else {
            Log.d(TAG, "launching app with new session");
            try {
                PendingResult<Cast.ApplicationConnectionResult> pendingResult =
                        Cast.CastApi.launchApplication(
                                apiClient,
                                RECEIVER_ID,
                                false);

                pendingResult.setResultCallback(this);

            } catch (Exception e) {
                Log.e(TAG, "Failed to launch application", e);
            }
        }

    }

    public void startDeviceDiscovery() {
        this.mediaRouter.addCallback(this.mediaRouteSelector, this.mediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
    }

    public void stopDeviceDiscovery() {
        this.mediaRouter.removeCallback(this.mediaRouterCallback);
    }

    private void reconnectChannels() {
        Log.w(TAG, "reconnecting to running app");
        if (this.apiClient != null) {
            Cast.CastApi.joinApplication(this.apiClient,
                    RECEIVER_ID,
                    this.sessionId)
                    .setResultCallback(this);
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "Connection suspended. Cause: " + cause);
        waitingForReconnect = true;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection Failed. Error Code: " + connectionResult.getErrorCode());
    }

    @Override
    public void onMetadataUpdated() {
        Log.d(TAG, "Metadata Updated.");
    }

    @Override
    public void onStatusUpdated() {
        Log.d(TAG, "Status Updated.");
    }

    @Override
    public void onResult(Cast.ApplicationConnectionResult appConnectionResult) {
        Status status = appConnectionResult.getStatus();
        if (status.isSuccess()) {
            if (this.routeToReconnect != null) {
                Log.d(TAG, "selecting route for reconnection");
                mediaRouter.selectRoute(routeToReconnect);
            }
            this.sessionId = appConnectionResult.getSessionId();

            Log.d(TAG, "received sessionId = " + this.sessionId);

            applicationStarted = true;

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("sessionId", this.sessionId);
            editor.apply();

            createChannel();

        } else {
            teardown();
        }

    }

    private void createChannel() {
        Log.d(TAG, "creating channel");

        mRemoteMediaPlayer = new RemoteMediaPlayer();
        mRemoteMediaPlayer.setOnStatusUpdatedListener(this);
        mRemoteMediaPlayer.setOnMetadataUpdatedListener(this);

        try {
            Cast.CastApi.setMessageReceivedCallbacks(apiClient,
                    mRemoteMediaPlayer.getNamespace(), mRemoteMediaPlayer);
        } catch (IOException e) {
            Log.e(TAG, "Exception while creating media channel", e);
        }
        mRemoteMediaPlayer
                .requestStatus(apiClient)
                .setResultCallback(
                        new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                            @Override
                            public void onResult(RemoteMediaPlayer.MediaChannelResult result) {
                                if (!result.getStatus().isSuccess()) {
                                    Log.e(TAG, "Failed to request status.");
                                }
                            }
                        }
                );
    }

    public void showOnDevice(Uri link) {
        if (mRemoteMediaPlayer == null || !isConnected()) {
            Toast.makeText(this.ctx, R.string.no_connection, Toast.LENGTH_LONG).show();
            return;
        }

        this.sendingContentCallback.onStartSending();

        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_PHOTO);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE, link.toString());
        MediaInfo mediaInfo = new MediaInfo.Builder(
                link.toString())
                .setContentType("image/jpeg") //FIXME infer mime type by URL extension
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(mediaMetadata)
                .build();
        try {
            mRemoteMediaPlayer.load(apiClient, mediaInfo, true)
                    .setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                        @Override
                        public void onResult(RemoteMediaPlayer.MediaChannelResult result) {

                            CastController.this.sendingContentCallback.onContentSent(result.getStatus().isSuccess());

                            if (result.getStatus().isSuccess()) {
                                Toast.makeText(CastController.this.ctx, R.string.load_ok, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(CastController.this.ctx, R.string.load_nok, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } catch (IllegalStateException e) {
            Log.e(TAG, "Problem occurred with media during loading", e);
        } catch (Exception e) {
            Log.e(TAG, "Problem opening media during loading", e);
        }
    }

    private boolean isConnected() {
        return apiClient != null && apiClient.isConnected();
    }

    public void setMediaRouteMenuItem(MenuItem mediaRouteMenuItem) {
        MediaRouteActionProvider mediaRouteActionProvider =
                (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteMenuItem);
        mediaRouteActionProvider.setRouteSelector(this.mediaRouteSelector);
    }

    private class MediaRouterCallback extends MediaRouter.Callback {

        @Override
        public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo info) {
            selectedDevice = CastDevice.getFromBundle(info.getExtras());
            routeId = info.getId();

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("routeId", routeId);
            editor.apply();

            connectToDevice(selectedDevice);
        }

        @Override
        public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo info) {
            Log.d(TAG, "Route unselected: " + info);
            teardown();
            selectedDevice = null;
        }

        @Override
        public void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo route) {
            super.onRouteAdded(router, route);
            Log.d(TAG, "Route added with Id : " + route.getId());
        }
    }

    private void reconnectToDeviceIfPossible() {
        Log.d(TAG, "Reconnecting if possible");

        if (hasPreviousSession()) {
            Log.d(TAG, "Previous session found for Route Id = " + routeId);

            for (MediaRouter.RouteInfo routeInfo : this.mediaRouter.getRoutes()) {
                Log.d(TAG, "Inspecting Route Id = " + routeInfo.getId()
                        + ". Route Infi =" + routeInfo);

                if (routeInfo.getId().equals(this.routeId)) {
                    this.routeToReconnect = routeInfo;
                    Log.d(TAG, "Route found: " + routeInfo);
                    break;
                }
            }

            if (this.routeToReconnect != null) {
                this.selectedDevice = CastDevice.getFromBundle(this.routeToReconnect.getExtras());
                this.waitingForReconnect = true;
                connectToDevice(this.selectedDevice);
            } else {
                if (!routeWatcher.done) {
                    routeWatcher.watch();
                } else {
                    Log.d(TAG, "no route found to previous session");
                    teardown();
                }
            }
        }
    }

    private RouteWatcher routeWatcher = new RouteWatcher();
    private class RouteWatcher {
        boolean done;

        public void watch() {
            Log.d(TAG, "Wait a bit for routes");

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Try to discover route again...");
                    RouteWatcher.this.done = true;
                    reconnectToDeviceIfPossible();
                }

            } , 5000);
        }
    }

    private boolean hasPreviousSession() {
        this.routeId = preferences.getString("routeId", null);
        this.sessionId = preferences.getString("sessionId", null);
        return this.routeId != null && this.sessionId != null;
    }

    private void connectToDevice(CastDevice castDevice) {
        Log.d(TAG, "connecting to " + castDevice);

        Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions
                .builder(castDevice, new CastListener());

        Log.d(TAG, "apiClient is null ? " + (apiClient == null));

        apiClient = new GoogleApiClient.Builder(this.ctx)
                .addApi(Cast.API, apiOptionsBuilder.build())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        Log.d(TAG, "apiClient connected? " + apiClient.isConnected());
        Log.d(TAG, "apiClient connecting? " + apiClient.isConnecting());

        apiClient.connect();
    }

    private class CastListener extends Cast.Listener {
        @Override
        public void onApplicationStatusChanged() {
            if (apiClient != null) {
                Log.d(TAG, "callback => " + this);

                Log.d(TAG, "onApplicationStatusChanged: "
                        + Cast.CastApi.getApplicationStatus(apiClient));
            }
        }

        @Override
        public void onVolumeChanged() {
            if (apiClient != null) {
                Log.d(TAG, "onVolumeChanged: " + Cast.CastApi.getVolume(apiClient));
            }
        }

        @Override
        public void onApplicationDisconnected(int errorCode) {
            Log.d(TAG, "Application Disconnected. errorCode: " + errorCode);
            teardown();
        }
    }

    public void teardown() {
        Log.d(TAG, "teardown");
        if (apiClient != null) {
            if (applicationStarted) {
                if (apiClient.isConnected()) {
                    stopChannel();
                }
                applicationStarted = false;
            }
            apiClient = null;
        }
        selectedDevice = null;
        waitingForReconnect = false;
        controllerStarted = true;

        sessionId = null;
        routeId = null;

        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("routeId");
        editor.remove("sessionId");
        editor.apply();
    }

    private void stopChannel() {
        Log.d(TAG, "stoping Channel and disconnecting");
        try {
            Cast.CastApi.stopApplication(apiClient, sessionId);
            if (mRemoteMediaPlayer != null) {
                Cast.CastApi.removeMessageReceivedCallbacks(
                        apiClient,
                        mRemoteMediaPlayer.getNamespace());
                mRemoteMediaPlayer = null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Exception while removing channel", e);
        }
        apiClient.disconnect();
    }

}

