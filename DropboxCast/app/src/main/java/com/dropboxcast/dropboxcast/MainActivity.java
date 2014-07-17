package com.dropboxcast.dropboxcast;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.dropbox.chooser.android.DbxChooser;
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

public class MainActivity extends ActionBarActivity implements
                        GoogleApiClient.ConnectionCallbacks,
                        GoogleApiClient.OnConnectionFailedListener,
                        ResultCallback<Cast.ApplicationConnectionResult>,
                        RemoteMediaPlayer.OnStatusUpdatedListener,
                        RemoteMediaPlayer.OnMetadataUpdatedListener {

    /*
    TODO

If the app couldn’t find a matching route from MediaRouter.getRoutes(),
it could be that the route is not discovered yet by the asynchronous discovery process.
Start a timer with a short life time, like 5 seconds, and start listening to the routes reported by MediaRouter.Callback.onRouteAdded.
If the desired route doesn’t show up before the timer expires, you can stop the reconnection process.

If the persisted route ID is matched before the timer expires, extract the CastDevice instance from that route.
Connect to the CastDevice instance using GoogleApiClient.connect() and once connected, call Cast.CastApi.joinApplication() with the persisted session ID.
If joining the application succeeds, it confirms that the same session is still running, and MediaRouter.selectRoute() can be called with the stored RouteInfo instance.
However, if joining the application fails, a different session is now running on the device so you need to disconnect from the CastDevice instance.
     */
    private static final String TAG = MainActivity.class.getSimpleName();

    private MediaRouter mediaRouter;
    private MediaRouteSelector mediaRouteSelector;
    private MediaRouterCallback mediaRouterCallback;

    private CastDevice selectedDevice;
    private RemoteMediaPlayer mRemoteMediaPlayer;
    private GoogleApiClient apiClient;

    private boolean mWaitingForReconnect;

    private boolean applicationStarted;

    private String sessionId;
    private String routeId;
    private MediaRouter.RouteInfo routeToReconnect;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new SourceSelectionFragment())
                    .commit();
        }

        mediaRouter = MediaRouter.getInstance(getApplicationContext());

        String categoryForCast = CastMediaControlIntent.categoryForCast(
                CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID);

        mediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(categoryForCast)
                .build();

        mediaRouterCallback = new MediaRouterCallback();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        reconnectIfPossible();
    }

    private void reconnectIfPossible() {
        if (hasPreviousSession()) {
            for (MediaRouter.RouteInfo routeInfo : mediaRouter.getRoutes()) {
                if (routeInfo.getId().equals(this.routeId)) {
                    routeToReconnect = routeInfo;
                    Log.d(TAG, "Route found: " + routeInfo);
                    break;
                }
            }

            if (routeToReconnect != null) {
                selectedDevice = CastDevice.getFromBundle(routeToReconnect.getExtras());
                mWaitingForReconnect = true;
                connectToDevice(selectedDevice);
            } else {
                teardown();
            }
        }
    }

    private boolean hasPreviousSession() {
        this.routeId = preferences.getString("routeId", null);
        this.sessionId = preferences.getString("sessionId", null);

        return this.routeId != null && this.sessionId != null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            DbxChooser.Result result = new DbxChooser.Result(data);
            showOnCastDevice(result.getLink());
        }
    }

    private void showOnCastDevice(Uri link) {
        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_PHOTO);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE, link.toString());
        MediaInfo mediaInfo = new MediaInfo.Builder(
                link.toString())
                .setContentType("image/jpeg")
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(mediaMetadata)
                .build();
        try {
            mRemoteMediaPlayer.load(apiClient, mediaInfo, true)
                    .setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                        @Override
                        public void onResult(RemoteMediaPlayer.MediaChannelResult result) {
                            if (result.getStatus().isSuccess()) {
                                Log.d(TAG, "Media loaded successfully");
                            } else {
                                Log.d(TAG, "Error on load media");
                            }
                        }
                    });
        } catch (IllegalStateException e) {
            Log.e(TAG, "Problem occurred with media during loading", e);
        } catch (Exception e) {
            Log.e(TAG, "Problem opening media during loading", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);

        MediaRouteActionProvider mediaRouteActionProvider =
                (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteMenuItem);
        mediaRouteActionProvider.setRouteSelector(mediaRouteSelector);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaRouter.addCallback(mediaRouteSelector, mediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
    }

    @Override
    protected void onPause() {
        if (isFinishing()) {
            mediaRouter.removeCallback(mediaRouterCallback);
        }
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaRouter.addCallback(mediaRouteSelector, mediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
    }

    @Override
    protected void onStop() {
        mediaRouter.removeCallback(mediaRouterCallback);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStatusUpdated() {

    }

    @Override
    public void onMetadataUpdated() {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        teardown();
    }

    private class MediaRouterCallback extends MediaRouter.Callback {

        @Override
        public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo info) {
            selectedDevice = CastDevice.getFromBundle(info.getExtras());
            routeId = info.getId();

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("routeId", routeId);
            editor.commit();

            connectToDevice(selectedDevice);
        }

        @Override
        public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo info) {
            teardown();
            selectedDevice = null;
        }
    }

    private void connectToDevice(CastDevice castDevice) {
        Log.d(TAG, "connecting to " + castDevice);

        Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions
                .builder(castDevice, new CastListener());

        apiClient = new GoogleApiClient.Builder(this)
                .addApi(Cast.API, apiOptionsBuilder.build())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        apiClient.connect();
    }

    private class CastListener extends Cast.Listener {
        @Override
        public void onApplicationStatusChanged() {
            if (apiClient != null) {
                Log.d(TAG, "onApplicationStatusChanged: "
                        + Cast.CastApi.getApplicationStatus(apiClient).);

                Cast.CastApi.getA

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
            teardown();
        }
    };

    @Override
    public void onConnected(Bundle connectionHint) {
        if (mWaitingForReconnect) {
            mWaitingForReconnect = false;
            reconnectChannels();
        } else {
            Log.d(TAG, "launching app with new session");
            try {
                PendingResult<Cast.ApplicationConnectionResult> pendingResult =
                        Cast.CastApi.launchApplication(
                                apiClient,
                                CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID,
                                false);

                pendingResult.setResultCallback(this);

            } catch (Exception e) {
                Log.e(TAG, "Failed to launch application", e);
            }
        }
    }

    @Override
    public void onResult(Cast.ApplicationConnectionResult result) {
        Status status = result.getStatus();
        if (status.isSuccess()) {
            if (this.sessionId != null) {
                Log.d(TAG, "selecting route for reconnection");
                mediaRouter.selectRoute(routeToReconnect);
            }

            sessionId = result.getSessionId();
            applicationStarted = true;

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("sessionId", sessionId);
            editor.commit();

            createChannel();

        } else {
            teardown();
        }
    }

    private void reconnectChannels() {
        Log.w(TAG, "reconnecting Channels ");
        Cast.CastApi.joinApplication(apiClient, CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID, sessionId)
                .setResultCallback(this);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "connection suspended");
        mWaitingForReconnect = true;
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

    private void teardown() {
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
        mWaitingForReconnect = false;
        sessionId = null;
        routeId = null;

        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("routeId");
        editor.remove("sessionId");
        editor.commit();
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
