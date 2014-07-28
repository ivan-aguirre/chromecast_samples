package com.dropboxcast.dropboxcast;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.dropbox.chooser.android.DbxChooser;

public class MainActivity extends ActionBarActivity {

    /*
     * FIXME: after killing process and reconnecting two callbacks are executed.
     * Reconnection worfklow may be broken.
     */
    
    /*
     * FIXME: kill Activity and unconnect. Button remains selected (platform bug)?
     */

    private static final String TAG = MainActivity.class.getSimpleName();

    private SourceSelectionFragment sourceSelectionFragment;

    private CastController castController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            sourceSelectionFragment = new SourceSelectionFragment();
            // Keep CastController under Activity recreation due configuration changes...
            sourceSelectionFragment.setRetainInstance(true);

            getFragmentManager().beginTransaction()
                    .add(R.id.container, sourceSelectionFragment, "selection")
                    .commit();
        } else {
            sourceSelectionFragment = (SourceSelectionFragment) getFragmentManager()
                    .findFragmentByTag("selection");
        }

        this.castController = sourceSelectionFragment.getCastController();
        this.castController.startOrReconnect(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            DbxChooser.Result result = new DbxChooser.Result(data);
            this.castController.showOnDevice(result.getLink());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);

        this.castController.setMediaRouteMenuItem(mediaRouteMenuItem);

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.castController.startDeviceDiscovery();
    }

    @Override
    protected void onPause() {
        if (isFinishing()) {
            this.castController.stopDeviceDiscovery();
            this.castController.teardown();
        }
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.castController.startDeviceDiscovery();
    }

    @Override
    protected void onStop() {
        this.castController.stopDeviceDiscovery();
        super.onStop();
    }
}
