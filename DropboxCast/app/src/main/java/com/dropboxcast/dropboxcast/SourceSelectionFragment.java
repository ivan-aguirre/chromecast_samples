package com.dropboxcast.dropboxcast;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.dropbox.chooser.android.DbxChooser;

public class SourceSelectionFragment extends Fragment implements CastController.SendingContentCallback{

    private static final String APP_KEY = "your Dropbox Key";
    private ProgressBar loadingBar;

    private CastController castController;

    public SourceSelectionFragment() {
        if (castController == null) {
            this.castController = new CastController(this);
        }
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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    public CastController getCastController() {
        return this.castController;
    }

    @Override
    public void onStartSending() {
        loadingBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onContentSent(boolean success) {
        loadingBar.setVisibility(View.INVISIBLE);
    }
}