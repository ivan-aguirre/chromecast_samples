package org.gcastsamples.dropboxvideocast;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;

import com.dropbox.chooser.android.DbxChooser;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.sample.castcompanionlibrary.cast.BaseCastManager;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.widgets.MiniController;

public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private SourceSelectionFragment sourceSelectionFragment; //TODO: remove this Fragment
    private VideoCastManager mVideoCastManager;
    private MiniController mMini;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            sourceSelectionFragment = new SourceSelectionFragment();
            sourceSelectionFragment.setRetainInstance(true);

            getFragmentManager().beginTransaction()
                    .add(R.id.container, sourceSelectionFragment, "selection")
                    .commit();
        } else {
            sourceSelectionFragment = (SourceSelectionFragment) getFragmentManager()
                    .findFragmentByTag("selection");
        }

        BaseCastManager.checkGooglePlayServices(this);
        mVideoCastManager = MyApplication.getVideoCastManager(this);

        mMini = (MiniController) findViewById(R.id.miniController1);
        mVideoCastManager.addMiniController(mMini);

        mVideoCastManager.reconnectSessionIfPossible(this, true, 5 /*seconds*/);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoCastManager = MyApplication.getVideoCastManager(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            DbxChooser.Result result = new DbxChooser.Result(data);

            Uri link = result.getLink();

            MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
            mediaMetadata.putString(MediaMetadata.KEY_TITLE, result.getName());
            mediaMetadata.putString(MediaMetadata.KEY_SUBTITLE, result.getName());
            mediaMetadata.putString(MediaMetadata.KEY_STUDIO, "");
            //TODO inform other fields...

            MediaInfo mediaInfo = new MediaInfo.Builder(link.toString())
                    .setContentType("video/mp4") //FIXME infer mime type by URL extension
                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                    .setMetadata(mediaMetadata)
                    .build();

            mVideoCastManager.startCastControllerActivity(this, mediaInfo, 0, true);
        }
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
}
