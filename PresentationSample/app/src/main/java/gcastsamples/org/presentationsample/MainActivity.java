package gcastsamples.org.presentationsample;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaRouter;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.support.v7.widget.RecyclerView.Adapter;


public class MainActivity extends Activity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private MediaRouter mMediaRouter;
    private ListPresentation mPresentation;

    private EditText edtData;
    private Spinner spinner;

    private DialogInterface.OnDismissListener mOnDismissListener = new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialog) {
            if (dialog == mPresentation) {
                Log.i(TAG, "Presentation was dismissed.");
                mPresentation = null;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_add).setOnClickListener(this);

        edtData = (EditText) findViewById(R.id.edt_data);
        spinner = (Spinner) findViewById(R.id.spn_city);

        mMediaRouter = (MediaRouter) getSystemService(Context.MEDIA_ROUTER_SERVICE);

        updatePresentation();
    }

    private final MediaRouter.SimpleCallback mMediaRouterCallback =
            new MediaRouter.SimpleCallback() {
                @Override
                public void onRouteSelected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
                    Log.d(TAG, "onRouteSelected: type=" + type + ", info=" + info);
                    updatePresentation();
                }

                @Override
                public void onRouteUnselected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
                    Log.d(TAG, "onRouteUnselected: type=" + type + ", info=" + info);
                    updatePresentation();
                }

                @Override
                public void onRoutePresentationDisplayChanged(MediaRouter router, MediaRouter.RouteInfo info) {
                    Log.d(TAG, "onRoutePresentationDisplayChanged: info=" + info);
                    updatePresentation();
                }
            };

    @Override
    protected void onResume() {
        super.onResume();
        mMediaRouter.addCallback(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, mMediaRouterCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMediaRouter.removeCallback(mMediaRouterCallback);
    }

    @Override
    public void onClick(View view) {
        if (mPresentation != null) {
            mPresentation.add(edtData.getText().toString(), spinner.getSelectedItemPosition());
        }
    }

    private void updatePresentation() {
        MediaRouter.RouteInfo route = mMediaRouter.getSelectedRoute(
                MediaRouter.ROUTE_TYPE_LIVE_VIDEO);

        Display presentationDisplay = route != null ? route.getPresentationDisplay() : null;

        if (mPresentation != null
                && mPresentation.getDisplay() != presentationDisplay) {
            Log.i(TAG, "Dismissing presentation because the current route no longer "
                    + "has a presentation display.");
            mPresentation.dismiss();
            mPresentation = null;
        }

        if (mPresentation == null && presentationDisplay != null) {
            Log.i(TAG, "Showing presentation on display: " + presentationDisplay);
            mPresentation = new ListPresentation(this, presentationDisplay);
            mPresentation.setOnDismissListener(mOnDismissListener);
            try {
                mPresentation.show();
            } catch (WindowManager.InvalidDisplayException ex) {
                Log.w(TAG, "Couldn't show presentation!  Display was removed in "
                        + "the meantime.", ex);
                mPresentation = null;
            }
        }
    }
}
