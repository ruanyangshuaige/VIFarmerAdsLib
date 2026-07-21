package com.vifarmer.ads.lib.ads.banner_ads;

import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.vifarmer.ads.lib.admob.Admob;
import com.vifarmer.ads.lib.ads.splash_ads.AsyncSplash;
import com.vifarmer.ads.lib.callback.BannerCallback;

public class BannerManager implements LifecycleEventObserver {
    private static final String TAG = "BannerManager";
    private final BannerBuilder builder;
    private Activity currentActivity;
    private final LifecycleOwner lifecycleOwner;
    private boolean isReloadAds = false;
    private boolean isAlwaysReloadOnResume = false;
    private long intervalReloadBanner = 0;
    private Context context;
    private int adWidth;
    private boolean isLoadBannerFragment = false;
    private final String remoteKey;
    private Boolean isLoadWaterFallMultiKeyAds = false;

    public BannerManager(@NonNull Activity currentActivity, LifecycleOwner lifecycleOwner, BannerBuilder builder, String remoteKey) {
        this.isLoadBannerFragment = false;
        this.builder = builder;
        this.currentActivity = currentActivity;
        this.remoteKey = remoteKey;
        this.lifecycleOwner = lifecycleOwner;
        this.lifecycleOwner.getLifecycle().addObserver(this);
    }

    public BannerManager(Context context, int adWidth, LifecycleOwner lifecycleOwner, BannerBuilder builder, String remoteKey) {
        this.isLoadBannerFragment = true;
        this.builder = builder;
        this.context = context;
        this.adWidth = adWidth;
        this.remoteKey = remoteKey;
        this.lifecycleOwner = lifecycleOwner;
        this.lifecycleOwner.getLifecycle().addObserver(this);
    }


    public BannerManager(@NonNull Activity currentActivity, LifecycleOwner lifecycleOwner, BannerBuilder builder, Boolean isLoadWaterFallMultiKeyAds) {
        this.isLoadBannerFragment = false;
        this.builder = builder;
        this.currentActivity = currentActivity;
        this.remoteKey = "";
        this.isLoadWaterFallMultiKeyAds = isLoadWaterFallMultiKeyAds;
        this.lifecycleOwner = lifecycleOwner;
        this.lifecycleOwner.getLifecycle().addObserver(this);
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        switch (event) {
            case ON_CREATE:
                Log.d(TAG, "onStateChanged: ON_CREATE");
                reloadAdNow();
                break;
            case ON_RESUME:
                Log.d(TAG, "onStateChanged: resume");
                break;
            case ON_PAUSE:
                Log.d(TAG, "onStateChanged: ON_PAUSE");
                break;
            case ON_DESTROY:
                Log.d(TAG, "onStateChanged: ON_DESTROY");
                if (builder.getFrContainer() != null) {
                    builder.getFrContainer().removeAllViews();
                }
                if (builder.bannerAdViewMain != null) {
                    builder.bannerAdViewMain.destroy();
                }
                this.lifecycleOwner.getLifecycle().removeObserver(this);
                break;
        }
    }

    private void loadBanner(FrameLayout frContainer) {
        Log.d(TAG, "loadBanner: " + builder.getListIdAdMain());
        if (Admob.getInstance().getShowAllAds()) {
            if(AsyncSplash.Companion.getInstance().getLoadWaterfallBannerMultiKetAdsIds()){
                Admob.getInstance().loadBannerAdsMultiKeys(
                        currentActivity,
                        builder.getListIdAdMain(),
                        frContainer,
                        builder.getCallBack());
            }else {
                Admob.getInstance().loadBannerAds(currentActivity, builder.getListIdAdMain(), frContainer, builder.getCallBack(),  remoteKey);
            }
        } else {
            frContainer.setVisibility(View.GONE);
        }
    }

    private void loadBannerFragment(FrameLayout frContainer) {
        Log.d(TAG, "loadBanner: " + builder.getListIdAdMain());
        if (Admob.getInstance().getShowAllAds()) {
            Admob.getInstance().loadBannerAds(context, adWidth, builder.getListIdAdMain(), frContainer, builder.getCallBack(),  remoteKey);
        } else {
            frContainer.setVisibility(View.GONE);
        }
    }

    public void setReloadAds() {
        isReloadAds = true;
    }

    public void reloadAdNow() {
        if (builder.useNewAdLoading) {
            loadNewAdFormat();
        } else {
            loadOldAdFormat();
        }
    }

    private void loadNewAdFormat() {
        if (!Admob.getInstance().checkCondition(currentActivity, remoteKey)) {
            if (builder.getFrContainer() != null) {
                builder.getFrContainer().removeAllViews();
            }
            return;
        }
        loadMainBanner();
    }

    private void loadMainBanner() {
        if (builder.bannerAdViewMain != null) {
            builder.bannerAdViewMain.destroy();
        }
        builder.bannerAdViewMain = Admob.getInstance().loadBannerAdsWithoutShow(currentActivity, builder.getListIdAdMain(), new BannerCallback() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                builder.getCallBack().onAdLoaded();
                Log.d(TAG, "onAdLoaded: Main");
                if (builder.getFrContainer() != null) {
                    builder.getFrContainer().addView(builder.bannerAdViewMain);
                    builder.getFrContainer().removeView(builder.shimmerBanner);
                }
            }

            @Override
            public void onAdFailedToLoad() {
                super.onAdFailedToLoad();
                builder.getCallBack().onAdFailedToLoad();
                Log.d(TAG, "onAdFailedToLoad: Main");
                builder.getFrContainer().removeView(builder.shimmerBanner);
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                builder.getCallBack().onAdImpression();
                Log.d(TAG, "onAdImpression: Main");
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                builder.getCallBack().onAdClicked();
                Log.d(TAG, "onAdClicked: Main");
            }
        }, remoteKey);
    }


    private void loadOldAdFormat() {
        if (isLoadBannerFragment) {
            loadBannerFragment(builder.getFrContainer());
        } else {
            loadBanner(builder.getFrContainer());
        }
    }

    public void setAlwaysReloadOnResume(boolean isAlwaysReloadOnResume) {
        this.isAlwaysReloadOnResume = isAlwaysReloadOnResume;
    }

}
