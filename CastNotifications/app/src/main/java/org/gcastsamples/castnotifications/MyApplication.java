package org.gcastsamples.castnotifications;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.callbacks.VideoCastConsumerImpl;

public class MyApplication extends Application {

    private static VideoCastManager mCastMgr;

    public static VideoCastManager getVideoCastManager(Context ctx) {
        if (null == mCastMgr) {
            mCastMgr = VideoCastManager.initialize(ctx,
                    "C22292BA",
                    null,  // Player Control, null == uses lib's default
                    "urn:x-cast:org.gcastsamples.castnotifications");

            mCastMgr.enableFeatures(VideoCastManager.FEATURE_DEBUGGING);

            mCastMgr.addVideoCastConsumer(new VideoCastConsumerImpl(){
                @Override
                public void onDataMessageSendFailed(int errorCode) {
                    Log.e("Casting", "Send failed with error code = " + errorCode);
                }

                @Override
                public void onDataMessageReceived(String message) {
                    Log.e("Casting", "Sent!!");
                }
            });
        }
        mCastMgr.setContext(ctx);
        return mCastMgr;
    }
}
