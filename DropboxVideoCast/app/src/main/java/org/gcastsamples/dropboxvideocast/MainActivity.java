package org.gcastsamples.dropboxvideocast;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.View;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.sample.castcompanionlibrary.cast.BaseCastManager;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.widgets.MiniController;

public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private VideoCastManager mVideoCastManager;
    private MiniController mMini;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BaseCastManager.checkGooglePlayServices(this);
        mVideoCastManager = MyApplication.getVideoCastManager(this);

        mVideoCastManager.reconnectSessionIfPossible(this, true, 5 /*seconds*/);

        findViewById(R.id.btn_cast).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoCastManager = MyApplication.getVideoCastManager(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        mVideoCastManager.addMediaRouterButton(menu, R.id.media_route_menu_item);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideoCastManager.removeMiniController(mMini);
    }

    @Override
    public void onClick(View view) {
        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE, "Title: Teste");
        mediaMetadata.putString(MediaMetadata.KEY_SUBTITLE, "Subtitle: Teste");
        mediaMetadata.putString(MediaMetadata.KEY_STUDIO, "Ivan de Aguirre Productions");

        MediaInfo mediaInfo = new MediaInfo.Builder("https://d2k4ls0ga9ks2.cloudfront.net/VID_20140727_225510282.mp4")
                .setContentType("video/mp4")
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(mediaMetadata)
                .build();

        mVideoCastManager.startCastControllerActivity(this, mediaInfo, 0, true);
    }
}
