package org.gcastsamples.plotandcast;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.google.sample.castcompanionlibrary.cast.BaseCastManager;
import com.google.sample.castcompanionlibrary.cast.DataCastManager;

import static android.view.View.OnClickListener;


public class MainActivity extends ActionBarActivity implements OnClickListener{

    private DataCastManager mDataCastManager;

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BaseCastManager.checkGooglePlayServices(this);

        mDataCastManager = MyApplication.getDataCastManager(this);
        mDataCastManager.reconnectSessionIfPossible(this, true, 5 /*seconds*/);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDataCastManager = MyApplication.getDataCastManager(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        mDataCastManager.addMediaRouterButton(menu, R.id.media_route_menu_item);
        return true;
    }

    @Override
    public void onClick(View view) {
        try {
            mDataCastManager.sendDataMessage("{\"funct\" : \"return Math.sin(x);\"," +
                            "\"begin\" : -10," +
                            "\"end\" : +10," +
                            "\"increment\" : 0.5}",
                    MyApplication.NAME_SPACE);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            Toast.makeText(this, R.string.error_sending, Toast.LENGTH_LONG).show();
        }
    }
}
