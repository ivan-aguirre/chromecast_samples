package org.gcastsamples.dropboxvideocast;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.dropbox.chooser.android.DbxChooser;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;

public class SourceSelectionFragment extends Fragment {

    private static final String APP_KEY = "ubphzyrw12f9ecz"; //FIXME remover da build
    private ProgressBar loadingBar;

    public SourceSelectionFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        final DbxChooser mChooser = new DbxChooser(APP_KEY);

        Button mChooserButton = (Button) rootView.findViewById(R.id.btn_dropbox);
        mChooserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChooser.forResultType(DbxChooser.ResultType.DIRECT_LINK)
                        .launch(getActivity(), 0);
            }
        });

        loadingBar = (ProgressBar) rootView.findViewById(R.id.loadingBar);
        loadingBar.setVisibility(View.INVISIBLE);

        return rootView;
    }


}