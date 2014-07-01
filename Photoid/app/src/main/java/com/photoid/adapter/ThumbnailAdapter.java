package com.photoid.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;

import com.photoid.widget.SquaredImageView;
import com.squareup.picasso.Picasso;


public class ThumbnailAdapter extends CursorAdapter {

    public ThumbnailAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return new SquaredImageView(context);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        SquaredImageView imageView = (SquaredImageView) view;
        Uri uri = Uri.withAppendedPath(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                cursor.getString(0));
        Picasso.with(context).load(uri).into(imageView);
    }
}
