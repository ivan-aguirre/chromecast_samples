package com.dropboxcast.dropboxcast;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.dropbox.chooser.android.DbxChooser;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            DbxChooser.Result result = new DbxChooser.Result(data);
            Log.d(MainActivity.class.getSimpleName(), "Link to selected file: " + result.getLink());
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private static final String APP_KEY = "ubphzyrw12f9ecz";
        private Button mChooserButton;
        private DbxChooser mChooser;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            mChooser = new DbxChooser(APP_KEY);

            mChooserButton = (Button) rootView.findViewById(R.id.btn_dropbox);
            mChooserButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mChooser.forResultType(DbxChooser.ResultType.DIRECT_LINK)
                            .launch(getActivity(), 0);
                }
            });

            return rootView;
        }
    }
}
