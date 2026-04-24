package com.diamondguide.redeemcode.ffftips;

import androidx.annotation.NonNull;

import com.vifarmer.ads.lib.ads.admob.Admob;
import com.vifarmer.ads.lib.ads.app_open_ads.AppOpenManager;
import com.vifarmer.ads.lib.application.AdsApplication;

public class Application extends AdsApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        AppOpenManager.getInstance().disableAppResumeWithActivity(SplashActivity.class);
        Admob.getInstance().setTokenEventAdjust("xxxxxx");
    }

    @NonNull
    @Override
    public String getAppTokenAdjust() {
        return null;
    }

    @NonNull
    @Override
    public String getFacebookID() {
        return null;
    }
}
