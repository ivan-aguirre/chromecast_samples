package com.dropboxcast.dropboxcast;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.dropbox.chooser.android.DbxChooser;

public class SourceSelectionFragment extends Fragment {

    private static final String APP_KEY = "ubphzyrw12f9ecz";
    private Button mChooserButton;
    private DbxChooser mChooser;

    public SourceSelectionFragment() {
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