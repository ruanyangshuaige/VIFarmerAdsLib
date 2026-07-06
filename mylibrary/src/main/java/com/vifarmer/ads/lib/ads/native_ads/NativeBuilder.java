package com.vifarmer.ads.lib.ads.native_ads;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.vifarmer.ads.lib.R;
import com.vifarmer.ads.lib.admob.AdmobApi;
import com.vifarmer.ads.lib.callback.NativeCallback;

import java.util.ArrayList;
import java.util.List;

public class NativeBuilder {
    private static final String TAG = "NativeBuilder";
    private NativeCallback callback = new NativeCallback();
    List<String> listIdAdMain = new ArrayList<>();
    NativeAdView nativeAdViewMain;
    NativeAdView nativeMetaAdView;
    ShimmerFrameLayout shimmerFrameLayout;
    private FrameLayout flAd;
    private int layoutNativeAdmob;
    private int layoutNativeMeta;
    private int layoutShimmerNative;

    public NativeBuilder(Context context, FrameLayout flAd, @LayoutRes int idLayoutShimmer, @LayoutRes int idLayoutNative, @LayoutRes int idLayoutNativeMeta) {
        setLayoutAds(context, flAd, idLayoutShimmer, idLayoutNative, idLayoutNativeMeta);
    }

    private void setLayoutAds(Context context, FrameLayout flAd, @LayoutRes int idLayoutShimmer, @LayoutRes int idLayoutNative, @LayoutRes int idLayoutNativeMeta) {
        this.flAd = flAd;
        this.layoutNativeAdmob = idLayoutNative;
        this.layoutNativeMeta = idLayoutNativeMeta;
        this.layoutShimmerNative = idLayoutShimmer;

        View _nativeAdView = LayoutInflater.from(context).inflate(idLayoutNative, null);
        View _nativeMetaAdView = LayoutInflater.from(context).inflate(idLayoutNativeMeta, null);
        View _shimmerFrameLayout = LayoutInflater.from(context).inflate(idLayoutShimmer, null);

        //layout native admob
        if (_nativeAdView instanceof NativeAdView) {
            nativeAdViewMain = (NativeAdView) LayoutInflater.from(context).inflate(idLayoutNative, null);
        } else {
            layoutNativeAdmob = com.vifarmer.ads.lib.R.layout.ads_native_large;
            nativeAdViewMain = (NativeAdView) LayoutInflater.from(context).inflate(com.vifarmer.ads.lib.R.layout.ads_native_large, null);
        }
        //layout native meta
        if (_nativeMetaAdView instanceof NativeAdView) {
            nativeMetaAdView = (NativeAdView) LayoutInflater.from(context).inflate(idLayoutNativeMeta, null);
        } else {
            layoutNativeMeta = R.layout.ads_native_meta_large;
            nativeMetaAdView = (NativeAdView) LayoutInflater.from(context).inflate(R.layout.ads_native_meta_large, null);
        }
        //shimmer native
        if (_shimmerFrameLayout instanceof ShimmerFrameLayout) {
            shimmerFrameLayout = (ShimmerFrameLayout) LayoutInflater.from(context).inflate(idLayoutShimmer, null);
        } else {
            layoutShimmerNative = R.layout.ads_shimmer_large;
            shimmerFrameLayout = (ShimmerFrameLayout) LayoutInflater.from(context).inflate(R.layout.ads_shimmer_large, null);
        }
    }

    public int getLayoutShimmerNative() {
        return this.layoutShimmerNative;
    }

    public int getLayoutNativeAdmob() {
        return this.layoutNativeAdmob;
    }

    public int getLayoutNativeMeta() {
        return this.layoutNativeMeta;
    }

    public FrameLayout getFlAd() {
        return this.flAd;
    }

    public List<String> getListIdAdMain() {
        return this.listIdAdMain;
    }

    public void setListIdAdMain(List<String> listIdAd) {
        this.listIdAdMain.clear();
        this.listIdAdMain.addAll(listIdAd);
    }

    public void setListIdAdMainByListKey(List<String> listKey) {
        this.listIdAdMain.clear();
        for (String key : listKey) {
            this.listIdAdMain.addAll(AdmobApi.getInstance().getListIDByName(key));
        }
    }

    public void setListIdAdMain(String nameIdAd) {
        this.listIdAdMain.clear();
        this.listIdAdMain.addAll(AdmobApi.getInstance().getListIDByName(nameIdAd));
    }

    public NativeCallback getCallback() {
        return callback;
    }

    public void setCallback(NativeCallback callback) {
        this.callback = callback;
    }
}
