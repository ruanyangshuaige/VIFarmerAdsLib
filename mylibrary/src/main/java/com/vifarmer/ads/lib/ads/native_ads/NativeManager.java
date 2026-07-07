package com.vifarmer.ads.lib.ads.native_ads;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.ads.nativead.NativeAd;
import com.vifarmer.ads.lib.admob.Admob;
import com.vifarmer.ads.lib.admob.admob_interface.IOnAdsImpression;

public class NativeManager implements LifecycleEventObserver {
    private static final String TAG = "NativeManager";
    private final NativeBuilder builder;
    private final Context context;
    private final LifecycleOwner lifecycleOwner;
    private String remoteKey;
    private NativeAd myNativeAdMain;
    private Boolean isLoadWaterFall = false;

    public NativeManager(@NonNull Context context, LifecycleOwner lifecycleOwner, NativeBuilder builder, String remoteKey) {
        this.builder = builder;
        this.context = context;
        this.remoteKey = remoteKey;
        this.lifecycleOwner = lifecycleOwner;
        this.lifecycleOwner.getLifecycle().addObserver(this);
    }

    public NativeManager(@NonNull Context context, LifecycleOwner lifecycleOwner, NativeBuilder builder, Boolean isLoadWaterFall) {
        this.builder = builder;
        this.context = context;
        this.lifecycleOwner = lifecycleOwner;
        this.lifecycleOwner.getLifecycle().addObserver(this);
        this.isLoadWaterFall = isLoadWaterFall;
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        switch (event) {
            case ON_CREATE:
                Log.d(TAG, "onStateChanged: ON_CREATE");
                loadNativeFloor();
                break;
            case ON_RESUME:
                Log.d(TAG, "onStateChanged: ON_RESUME");
                break;
            case ON_PAUSE:
                Log.d(TAG, "onStateChanged: ON_PAUSE");
                break;
            case ON_DESTROY:
                Log.d(TAG, "onStateChanged: ON_DESTROY");
                if (myNativeAdMain != null) {
                    myNativeAdMain.destroy();
                }
                this.lifecycleOwner.getLifecycle().removeObserver(this);
                break;
        }
    }

    private void loadNativeFloor() {
        if (myNativeAdMain != null) {
            myNativeAdMain.destroy();
        }
        if (!builder.getListIdAdMain().isEmpty()) {
            if(isLoadWaterFall){
                myNativeAdMain = Admob.getInstance().loadNativeAdsWaterfall(context, builder.getListIdAdMain(), builder.getFlAd(), builder.getLayoutNativeAdmob(), builder.getLayoutNativeMeta(), builder.getLayoutShimmerNative(), true, builder.getCallback(), new IOnAdsImpression() {
                    @Override
                    public void onAdsImpression() {

                    }
                });
            }else {
                myNativeAdMain = Admob.getInstance().loadNativeAds(context, builder.getListIdAdMain(), builder.getFlAd(), builder.getLayoutNativeAdmob(), builder.getLayoutNativeMeta(), builder.getLayoutShimmerNative(), true, builder.getCallback(), new IOnAdsImpression() {
                    @Override
                    public void onAdsImpression() {

                    }
                }, remoteKey);
            }

        }
    }
}
