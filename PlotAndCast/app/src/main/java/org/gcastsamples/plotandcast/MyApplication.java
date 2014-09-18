package org.gcastsamples.plotandcast;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.common.api.Status;
import com.google.sample.castcompanionlibrary.cast.DataCastManager;
import com.google.sample.castcompanionlibrary.cast.callbacks.DataCastConsumerImpl;

public class MyApplication extends Application {

    private static DataCastManager mCastMgr;

    public static final String NAME_SPACE = "urn:x-cast:org.gcastsamples.plotandcast";

    public static DataCastManager getDataCastManager(Context ctx) {
        if (null == mCastMgr) {
            mCastMgr = DataCastManager.initialize(ctx, "C22292BA", NAME_SPACE);

            mCastMgr.enableFeatures(DataCastManager.FEATURE_DEBUGGING);

            mCastMgr.addDataCastConsumer(new DataCastConsumerImpl() {
                @Override
                public void onMessageReceived(CastDevice castDevice, String namespace, String message) {
                    Log.e("Casting", namespace + "=" + message);
                }

                @Override
                public void onMessageSendFailed(Status status) {
                    Log.e("Casting", "Send failed with status = " + status);
                }
            });
        }
        mCastMgr.setContext(ctx);
        return mCastMgr;
    }
}
