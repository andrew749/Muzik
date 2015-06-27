package com.acod.play.app;

import android.content.Context;

import com.google.android.gms.cast.Cast;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by andrewcodispoti on 2015-06-26.
 */
public class Muzik extends android.app.Application {
    public static GoogleApiClient mApiClient;
    private Context mContext;
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
