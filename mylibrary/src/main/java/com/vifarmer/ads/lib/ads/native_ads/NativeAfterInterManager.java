package com.vifarmer.ads.lib.ads.native_ads;

import android.app.Activity;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.vifarmer.ads.lib.R;
import com.vifarmer.ads.lib.Utils.EventTrackingHelper;
import com.vifarmer.ads.lib.admob.Admob;
import com.vifarmer.ads.lib.admob.AdmobApi;
import com.vifarmer.ads.lib.ads.splash_ads.AsyncSplash;
import com.vifarmer.ads.lib.callback.NativeCallback;
import com.vifarmer.ads.lib.view.NativeAfterInterActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NativeAfterInterManager {
    private static final String TAG = "Admob";
    public static final Map<String, NativeAd> mapNativeAdsAfterInter = new HashMap<>();

    public static void preloadNativeAfterInter(Activity activity, String adsKey, String remoteKey) {
        String[] listKeys = AsyncSplash.Companion.getInstance().getListKeyNativeAfterInter().toArray(new String[0]);
        boolean isLoadWaterfallMultiKeyAdsIds = AsyncSplash.Companion.getInstance().getLoadWaterfallNativeFullSplashMultiKeyAdsIds();
        if (isLoadWaterfallMultiKeyAdsIds) {
            preloadNativeAfterInter(activity, adsKey, remoteKey, listKeys, null);
        } else {
            preloadNativeAfterInter(activity, adsKey, remoteKey, null, null);
        }
    }

    public static void preloadNativeAfterInter(Activity activity, String adsKey, String remoteKey, String[] listKeys, String[] listRemoteKeys) {
        if (listKeys != null && listKeys.length > 0) {
            preloadDynamicWaterfallNativeAfterInter(activity, adsKey, remoteKey, listKeys, listRemoteKeys);
        } else {
            NativeAfterInterActivity.Companion.setTimeDelayShowXButton(AsyncSplash.Companion.getInstance().getTimeOutShowXButtonNativeAfterInter());
            NativeAfterInterActivity.Companion.setAdsKey(adsKey);
            NativeAfterInterActivity.Companion.setRemoteKey(remoteKey);
            NativeAfterInterActivity.Companion.setListKeys(null);
            NativeAfterInterActivity.Companion.setListRemoteKeys(null);
            Log.d(TAG, "NativeAfterInterManager: preloadNativeAfterInter - adskey = " + mapNativeAdsAfterInter.get(adsKey));
            if (mapNativeAdsAfterInter.get(adsKey) == null) {
                Log.d(TAG, "NativeAfterInterManager: 1.preloadNativeAfterInter." + AdmobApi.getInstance().getListIDByName(adsKey));
                EventTrackingHelper.logEvent(activity, remoteKey + "_preload_request");
                Admob.getInstance().loadNativeAds(
                        activity,
                        AdmobApi.getInstance().getListIDByName(adsKey),
                        new NativeCallback() {
                            @Override
                            public void onNativeAdLoaded(NativeAd nativeAd) {
                                super.onNativeAdLoaded(nativeAd);
                                mapNativeAdsAfterInter.put(adsKey, nativeAd);
                                Log.d(TAG, "NativeAfterInterManager: onNativeAdLoaded: " + mapNativeAdsAfterInter);
                                EventTrackingHelper.logEvent(activity, remoteKey + "_preload_success");
                            }

                            @Override
                            public void onAdFailedToLoad(LoadAdError loadAdError) {
                                super.onAdFailedToLoad(loadAdError);
                                mapNativeAdsAfterInter.put(adsKey, null);
                                Log.d(TAG, "NativeAfterInterManager: 1.onAdFailedToLoad: " + loadAdError.getMessage());
                                EventTrackingHelper.logEvent(activity, remoteKey + "_preload_failed_" + loadAdError.getMessage());
                            }
                        }, remoteKey
                );
            }
        }
    }

    /**
     * @param listKeys Danh sách các key đầy đủ theo thứ tự ưu tiên
     */
    public static void preloadDynamicWaterfallNativeAfterInter(Activity activity, String adsKey, String remoteKey, String[] listKeys, String[] listRemoteKeys) {
        NativeAfterInterActivity.Companion.setTimeDelayShowXButton(AsyncSplash.Companion.getInstance().getTimeOutShowXButtonNativeAfterInter());
        NativeAfterInterActivity.Companion.setAdsKey(adsKey);
        NativeAfterInterActivity.Companion.setRemoteKey(remoteKey);
        NativeAfterInterActivity.Companion.setListKeys(listKeys);
        NativeAfterInterActivity.Companion.setListRemoteKeys(listRemoteKeys);

        loadNativeWaterfall(activity, adsKey, listKeys, listRemoteKeys, 0);
    }

    private static void loadNativeWaterfall(Activity activity, String adsKey, String[] listKeys, String[] listRemoteKeys, int index) {
        if (listKeys == null || index >= listKeys.length) return;

        String targetKey = listKeys[index];
        String targetRemoteKey = (listRemoteKeys != null && index < listRemoteKeys.length) ? listRemoteKeys[index] : targetKey;

        List<String> ids = AdmobApi.getInstance().getListIDByName(targetKey);

        boolean adExists = false;
        for (String key : listKeys) {
            if (mapNativeAdsAfterInter.get(key) != null) {
                adExists = true;
                break;
            }
        }

        if (adExists) {
            Log.d(TAG, "NativeAfterInterManager: Already has ad in waterfall for " + adsKey);
            return;
        }

        if (ids == null || ids.isEmpty()) {
            Log.d(TAG, "NativeAfterInterManager: No IDs for " + targetKey + ", trying next key...");
            loadNativeWaterfall(activity, adsKey, listKeys, listRemoteKeys, index + 1);
            return;
        }

        Log.d(TAG, "NativeAfterInterManager: Loading native for key: " + targetKey + " with remoteKey: " + targetRemoteKey);
        EventTrackingHelper.logEvent(activity, targetRemoteKey + "_preload_request");
        Admob.getInstance().loadNativeAds(
                activity,
                ids,
                new NativeCallback() {
                    @Override
                    public void onNativeAdLoaded(NativeAd nativeAd) {
                        super.onNativeAdLoaded(nativeAd);
                        mapNativeAdsAfterInter.put(targetKey, nativeAd);
                        Log.d(TAG, "NativeAfterInterManager: onNativeAdLoaded for key: " + targetKey);
                        EventTrackingHelper.logEvent(activity, targetRemoteKey + "_preload_success");
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        Log.d(TAG, "NativeAfterInterManager: Failed to load " + targetKey + ", trying next key...");
                        EventTrackingHelper.logEvent(activity, targetRemoteKey + "_preload_failed_" + loadAdError.getMessage());
                        loadNativeWaterfall(activity, adsKey, listKeys, listRemoteKeys, index + 1);
                    }
                }, targetRemoteKey
        );
    }

    public static void showPreloadNativeAfterInter(int timeDelayShowXButton, FrameLayout fr, Activity activity, String adsKey, String remoteKey, OnCloseNativeListener listener) {
        Log.d(TAG, "NativeAfterInterManager: showPreloadNativeAfterInter: adsKey = " + adsKey);
        int idLayoutNative = R.layout.native_after_inter;
        
        String[] listKeys = NativeAfterInterActivity.Companion.getListKeys();
        String[] listRemoteKeys = NativeAfterInterActivity.Companion.getListRemoteKeys();
        NativeAd nativeAd = null;
        String loadedKey = adsKey;

        if (listKeys != null && listKeys.length > 0) {
            for (String key : listKeys) {
                nativeAd = mapNativeAdsAfterInter.get(key);
                if (nativeAd != null) {
                    loadedKey = key;
                    Log.d(TAG, "NativeAfterInterManager: Found preloaded ad with key: " + loadedKey);
                    break;
                }
            }
        } else {
            nativeAd = mapNativeAdsAfterInter.get(adsKey);
        }

        if (nativeAd != null) {
            Log.d(TAG, "NativeAfterInterManager: NativeAd Show");
            EventTrackingHelper.logEvent(activity, remoteKey + "_show");
            LayoutInflater layoutInflater = LayoutInflater.from(fr.getContext());

            NativeAdView adView = (NativeAdView) layoutInflater.inflate(idLayoutNative, fr, false);

            AppCompatButton btnClose = adView.findViewById(R.id.btn_close);
            ImageView imgClose = adView.findViewById(R.id.img_close);
            btnClose.setOnClickListener(view -> {
                EventTrackingHelper.logEvent(activity, remoteKey + "_close_click");
                if (listener != null) {
                    listener.onClose();
                }
            });
            imgClose.setOnClickListener(view -> {
                EventTrackingHelper.logEvent(activity, remoteKey + "_close_click");
                if (listener != null) {
                    listener.onClose();
                }
            });
            //if (Admob.getInstance().canCountTimeStartToShowXButtonNativeAfterInter) {
                new android.os.Handler(Looper.getMainLooper()).postDelayed(() -> imgClose.setVisibility(View.VISIBLE), timeDelayShowXButton);
            //}
            fr.removeAllViews();
            fr.addView(adView);
            Admob.getInstance().populateNativeAdView(nativeAd, adView);
            
            // Clear the specific key that was loaded
            mapNativeAdsAfterInter.put(loadedKey, null);
        } else {
            Log.d(TAG, "NativeAfterInterManager: NativeAd NULL onNext");
            EventTrackingHelper.logEvent(activity, remoteKey + "_show_fail_null");
            if (listener != null) {
                listener.onFail();
            }
        }

        // Trigger preload for the next time
        if (listKeys != null && listKeys.length > 0) {
            preloadDynamicWaterfallNativeAfterInter(activity, adsKey, remoteKey, listKeys, listRemoteKeys);
        } else {
            preloadNativeAfterInter(activity, adsKey, remoteKey);
        }

    }

    public interface OnCloseNativeListener {
        void onClose();

        void onFail();
    }
}
