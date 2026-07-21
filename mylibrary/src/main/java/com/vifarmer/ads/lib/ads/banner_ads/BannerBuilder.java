package com.vifarmer.ads.lib.ads.banner_ads;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.vifarmer.ads.lib.callback.BannerCallback;
import com.vifarmer.ads.lib.R;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;

public class BannerBuilder {
    private BannerCallback callBack = new BannerCallback();
    private FrameLayout frContainer;
    public boolean useNewAdLoading = false;
    public AdView bannerAdViewMain;
    public List<String> listIdAdMain = new ArrayList<>();
    public View shimmerBanner;

    public BannerBuilder(FrameLayout frContainer) {
        this.frContainer = frContainer;
    }

    public BannerBuilder(Activity activity, FrameLayout frContainer, boolean useNewAdLoading) {
        this.useNewAdLoading = useNewAdLoading;
        this.frContainer = frContainer;
        //Show loading shimmer
        shimmerBanner = LayoutInflater.from(activity).inflate(R.layout.layout_shimmer_banner, null);
        if (frContainer != null) {
            frContainer.addView(shimmerBanner);
        }
    }

    public BannerBuilder setCallBack(BannerCallback callBack) {
        this.callBack = callBack;
        return this;
    }

    public BannerCallback getCallBack() {
        return callBack;
    }

    public List<String> getListIdAdMain() {
        return listIdAdMain;
    }

    public void setListIdAdMain(List<String> listIdAdMain) {
        this.listIdAdMain = listIdAdMain;
    }



    public FrameLayout getFrContainer() {
        return frContainer;
    }

    public void setFrContainer(FrameLayout frContainer) {
        this.frContainer = frContainer;
    }
}