package org.gcastsamples.dropboxvideocast;

import android.app.Application;
import android.content.Context;

import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;

public class MyApplication extends Application {

    private static VideoCastManager mCastMgr;

    public static VideoCastManager getVideoCastManager(Context ctx) {
        if (null == mCastMgr) {
            mCastMgr = VideoCastManager.initialize(ctx,
                    CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID,
                    null,  // Player Control, null == uses lib's default
                    null); // Only for Custom Data Channel
            mCastMgr.enableFeatures(
                    VideoCastManager.FEATURE_LOCKSCREEN |
                    VideoCastManager.FEATURE_DEBUGGING);
        }
        mCastMgr.setContext(ctx);
        return mCastMgr;
    }

}
