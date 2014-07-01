package com.photoid.fragment;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.GridView;

import com.photoid.R;
import com.photoid.adapter.ThumbnailAdapter;

public class ImageGridFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    public ImageGridFragment() {
    }

    private GridView gridView;

    private ThumbnailAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_image_grid, container, false);

        adapter = new ThumbnailAdapter(getActivity(), null);
        gridView = (GridView) rootView.findViewById(R.id.gridView);
        gridView.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = { MediaStore.Images.Thumbnails._ID};
        return new CursorLoader(getActivity(),
                MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                projection,
                MediaStore.Images.Thumbnails.KIND + "=?",
                new String[]{String.valueOf(MediaStore.Images.Thumbnails.MINI_KIND)},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        adapter.swapCursor(null);
    }
}