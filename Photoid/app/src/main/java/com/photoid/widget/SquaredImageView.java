package com.photoid.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * https://github.com/square/picasso/blob/master/picasso-sample/src/main/java/com/example/picasso/SquaredImageView.java
 */
public class SquaredImageView extends ImageView {

    public SquaredImageView(Context context) {
        super(context);
    }

    public SquaredImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
    }
}