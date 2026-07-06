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
        String[] suffixes = AsyncSplash.Companion.getInstance().getSuffixesNativeAfterInter();
        preloadNativeAfterInter(activity, adsKey, suffixes, remoteKey);
    }

    public static void preloadNativeAfterInter(Activity activity, String adsKey, String[] suffixes, String remoteKey) {
        if (suffixes != null && suffixes.length > 0) {
            preloadDynamicWaterfallNativeAfterInter(activity, adsKey, suffixes, remoteKey);
        } else {
            NativeAfterInterActivity.Companion.setTimeDelayShowXButton(AsyncSplash.Companion.getInstance().getTimeOutShowXButtonNativeAfterInter());
            NativeAfterInterActivity.Companion.setAdsKey(adsKey);
            NativeAfterInterActivity.Companion.setRemoteKey(remoteKey);
            NativeAfterInterActivity.Companion.setSuffixes(null);
            Log.d(TAG, "NativeAfterInterManager: preloadNativeAfterInter - adskey = " + mapNativeAdsAfterInter.get(adsKey));
            if (mapNativeAdsAfterInter.get(adsKey) == null) {
                Log.d(TAG, "NativeAfterInterManager: 1.preloadNativeAfterInter." + AdmobApi.getInstance().getListIDByName(adsKey));
                Admob.getInstance().loadNativeAds(
                        activity,
                        AdmobApi.getInstance().getListIDByName(adsKey),
                        new NativeCallback() {
                            @Override
                            public void onNativeAdLoaded(NativeAd nativeAd) {
                                super.onNativeAdLoaded(nativeAd);
                                mapNativeAdsAfterInter.put(adsKey, nativeAd);
                                Log.d(TAG, "NativeAfterInterManager: onNativeAdLoaded: " + mapNativeAdsAfterInter);
                            }

                            @Override
                            public void onAdFailedToLoad(LoadAdError loadAdError) {
                                super.onAdFailedToLoad(loadAdError);
                                mapNativeAdsAfterInter.put(adsKey, null);
                                Log.d(TAG, "NativeAfterInterManager: 1.onAdFailedToLoad: " + loadAdError.getMessage());
                            }
                        }, remoteKey
                );
            }
        }
    }

    /**
     * @param baseAdsKey Tên key gốc (VD: "native_language")
     * @param suffixes   Mảng các hậu tố theo thứ tự ưu tiên giảm dần (VD: new String[]{"_high", "_mid", ""})
     */
    public static void preloadDynamicWaterfallNativeAfterInter(Activity activity, String baseAdsKey, String[] suffixes, String remoteKey) {
        preloadDynamicWaterfallNativeAfterInter(activity, baseAdsKey, suffixes, remoteKey, 0);
    }

    private static void preloadDynamicWaterfallNativeAfterInter(Activity activity, String baseAdsKey, String[] suffixes, String remoteKey, int index) {
        if (suffixes == null || index >= suffixes.length) return;

        NativeAfterInterActivity.Companion.setTimeDelayShowXButton(AsyncSplash.Companion.getInstance().getTimeOutShowXButtonNativeAfterInter());
        NativeAfterInterActivity.Companion.setAdsKey(baseAdsKey);
        NativeAfterInterActivity.Companion.setRemoteKey(remoteKey);
        NativeAfterInterActivity.Companion.setSuffixes(suffixes);

        // Kiểm tra xem đã có bất kỳ quảng cáo nào trong waterfall này được load chưa
        boolean alreadyHasAd = false;
        for (String suffix : suffixes) {
            if (mapNativeAdsAfterInter.get(baseAdsKey + suffix) != null) {
                alreadyHasAd = true;
                break;
            }
        }

        if (alreadyHasAd) {
            Log.d(TAG, "NativeAfterInterManager: Already has ad in waterfall for " + baseAdsKey);
            return;
        }

        String targetKey = baseAdsKey + suffixes[index];
        // use targetKey with remoteKey to check remote config
        String targetRemoteKey = targetKey; 

        List<String> ids = AdmobApi.getInstance().getListIDByName(targetKey);

        if (ids == null || ids.isEmpty()) {
            Log.d(TAG, "NativeAfterInterManager: No IDs for " + targetKey + ", trying next suffix...");
            preloadDynamicWaterfallNativeAfterInter(activity, baseAdsKey, suffixes, remoteKey, index + 1);
            return;
        }
        Log.d(TAG, "NativeAfterInterManager: Loading native for key: " + targetKey + "-with remoteKey: " + targetRemoteKey + "-ids Ads: "+ ids);
        Admob.getInstance().loadNativeAds(
                activity,
                ids,
                new NativeCallback() {
                    @Override
                    public void onNativeAdLoaded(NativeAd nativeAd) {
                        super.onNativeAdLoaded(nativeAd);
                        mapNativeAdsAfterInter.put(targetKey, nativeAd);
                        Log.d(TAG, "NativeAfterInterManager: onNativeAdLoaded for key: " + targetKey);
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        Log.d(TAG, "NativeAfterInterManager: Failed to load " + targetKey + ", trying next suffix...");
                        preloadDynamicWaterfallNativeAfterInter(activity, baseAdsKey, suffixes, remoteKey, index + 1);
                    }
                }, targetRemoteKey
        );
    }

    public static void showPreloadNativeAfterInter(int timeDelayShowXButton, FrameLayout fr, Activity activity, String adsKey, String remoteKey, OnCloseNativeListener listener) {
        Log.d(TAG, "NativeAfterInterManager: showPreloadNativeAfterInter: adsKey = " + adsKey);
        int idLayoutNative = R.layout.native_after_inter;
        
        String[] suffixes = NativeAfterInterActivity.Companion.getSuffixes();
        NativeAd nativeAd = null;
        String loadedKey = adsKey;

        if (suffixes != null) {
            for (String suffix : suffixes) {
                String targetKey = adsKey + suffix;
                nativeAd = mapNativeAdsAfterInter.get(targetKey);
                if (nativeAd != null) {
                    loadedKey = targetKey;
                    Log.d(TAG, "NativeAfterInterManager: Found preloaded ad with key: " + loadedKey);
                    break;
                }
            }
        } else {
            nativeAd = mapNativeAdsAfterInter.get(adsKey);
        }

        if (nativeAd != null) {
            Log.d(TAG, "NativeAfterInterManager: NativeAd Show");
            LayoutInflater layoutInflater = LayoutInflater.from(fr.getContext());

            NativeAdView adView = (NativeAdView) layoutInflater.inflate(idLayoutNative, fr, false);

            AppCompatButton btnClose = adView.findViewById(R.id.btn_close);
            ImageView imgClose = adView.findViewById(R.id.img_close);
            btnClose.setOnClickListener(view -> {
                if (listener != null) {
                    listener.onClose();
                }
            });
            imgClose.setOnClickListener(view -> {
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
            if (listener != null) {
                listener.onFail();
            }
        }

        // Trigger preload for the next time
        if (suffixes != null) {
            preloadDynamicWaterfallNativeAfterInter(activity, adsKey, suffixes, remoteKey);
        } else {
            preloadNativeAfterInter(activity, adsKey, remoteKey);
        }

    }

    public interface OnCloseNativeListener {
        void onClose();

        void onFail();
    }
}
