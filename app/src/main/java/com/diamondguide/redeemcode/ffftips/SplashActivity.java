package com.diamondguide.redeemcode.ffftips;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwnerKt;

import com.vifarmer.ads.lib.Utils.RemoteConfigHelper;
import com.vifarmer.ads.lib.admob.AdmobApi;
import com.vifarmer.ads.lib.callback.AppOpenCallback;
import com.vifarmer.ads.lib.callback.InterCallback;
import com.vifarmer.ads.lib.ads.splash_ads.AsyncSplash;
/*import com.amazic.library.iap.IAPManager;
import com.amazic.library.iap.ProductDetailCustom;*/
import com.vifarmer.ads.lib.update_app.UpdateApplicationManager;
import com.diamondguide.redeemcode.ffftips.databinding.ActivitySplashBinding;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.InstallStatus;

import java.util.ArrayList;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class SplashActivity extends AppCompatActivity {
    private ActivitySplashBinding binding;
    private InterCallback interCallback;
    private AppOpenCallback appOpenCallback;
    private String jsonIdAdsDefault = "["
            + "{\"id\":2001,\"package_name\":\"com.sticker.stickermaker.emoji.createsticker\",\"app_id\":\"ca-app-pub-3940256099942544~3347511713\",\"name\":\"inter_splash_2f\",\"ads_id\":\"ca-app-pub-3940256099942544/1033173712\"},"
            + "{\"id\":2002,\"package_name\":\"com.sticker.stickermaker.emoji.createsticker\",\"app_id\":\"ca-app-pub-3940256099942544~3347511713\",\"name\":\"inter_splash\",\"ads_id\":\"ca-app-pub-3940256099942544/1033173712\"},"
            + "{\"id\":2003,\"package_name\":\"com.sticker.stickermaker.emoji.createsticker\",\"app_id\":\"ca-app-pub-3940256099942544~3347511713\",\"name\":\"native_full_splash_2f\",\"ads_id\":\"ca-app-pub-3940256099942544/2247696110\"},"
            + "{\"id\":2004,\"package_name\":\"com.sticker.stickermaker.emoji.createsticker\",\"app_id\":\"ca-app-pub-3940256099942544~3347511713\",\"name\":\"native_full_splash\",\"ads_id\":\"ca-app-pub-3940256099942544/2247696110\"},"
            + "{\"id\":2005,\"package_name\":\"com.sticker.stickermaker.emoji.createsticker\",\"app_id\":\"ca-app-pub-3940256099942544~3347511713\",\"name\":\"native_full_2f\",\"ads_id\":\"ca-app-pub-3940256099942544/2247696110\"},"
            + "{\"id\":2006,\"package_name\":\"com.sticker.stickermaker.emoji.createsticker\",\"app_id\":\"ca-app-pub-3940256099942544~3347511713\",\"name\":\"native_full\",\"ads_id\":\"ca-app-pub-3940256099942544/2247696110\"},"
            + "{\"id\":2007,\"package_name\":\"com.sticker.stickermaker.emoji.createsticker\",\"app_id\":\"ca-app-pub-3940256099942544~3347511713\",\"name\":\"inter_all\",\"ads_id\":\"ca-app-pub-3940256099942544/1033173712\"}"
            + "]";
    public static AppUpdateManager appUpdateManager;
    public static InstallStateUpdatedListener installStateUpdatedListener;
    private boolean isHandleAsyncSplash = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        interCallback = new InterCallback() {
            @Override
            public void onNextAction() {
                super.onNextAction();
                startNextAct();
            }
        };

        appOpenCallback = new AppOpenCallback() {
            @Override
            public void onNextAction() {
                super.onNextAction();
                startNextAct();
            }
        };
        //User must update to the newest version to use the app
        UpdateApplicationManager.getInstance().init(this, new UpdateApplicationManager.IonUpdateApplication() {
            @Override
            public void onUpdateApplicationFail() {
                handleAsyncSplashJustOnce();
                Toast.makeText(SplashActivity.this, "Update Application Fail", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUpdateApplicationSuccess() {
                Toast.makeText(SplashActivity.this, "Update Application Success", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onMustNotUpdateApplication() {
                handleAsyncSplashJustOnce();
            }

            @Override
            public void requestUpdateFail() {
                handleAsyncSplashJustOnce();
            }
        });
        /*RemoteConfigHelper.getInstance().fetchAllKeysAndTypes(SplashActivity.this, () -> {
            Admob.getInstance().setShowAllAds(RemoteConfigHelper.getInstance().get_config(SplashActivity.this, RemoteConfigHelper.show_all_ads));
            Admob.getInstance().setTimeInterval(RemoteConfigHelper.getInstance().get_config_long(SplashActivity.this, RemoteConfigHelper.interval_between_interstitial) * 1000);
            Admob.getInstance().setTimeIntervalFromStart(RemoteConfigHelper.getInstance().get_config_long(SplashActivity.this, RemoteConfigHelper.interval_interstitial_from_start) * 1000);
            AsyncSplash.Companion.getInstance().setUseAppUpdateManager(true); // Do not recall remote config
            if (RemoteConfigHelper.getInstance().get_config(SplashActivity.this, "force_update_version")) {
                //UpdateApplicationManager.getInstance().setUseImmediateUpdate();
                UpdateApplicationManager.getInstance().setUseFlexibleUpdate();
                appUpdateManager = UpdateApplicationManager.getInstance().checkVersionPlayStore(
                        SplashActivity.this,
                        true,
                        false,
                        "\uD83D\uDE80 New Update Available!",
                        "Upgrade now for a smoother experience, bug fixes for better performance. ⚡",
                        "Update Now",
                        "No"
                );
            } else {
                handleAsync();
            }
        });*/
        UpdateApplicationManager.getInstance().setUseFlexibleUpdate();
        appUpdateManager = UpdateApplicationManager.getInstance().checkVersionPlayStore(
                SplashActivity.this,
                true,
                false,
                "\uD83D\uDE80 New Update Available!",
                "Upgrade now for a smoother experience, bug fixes for better performance. ⚡",
                "Update Now",
                "No"
        );
        installStateUpdatedListener = installState -> {
            if (installState.installStatus() == InstallStatus.DOWNLOADING ||
                    installState.installStatus() == InstallStatus.FAILED ||
                    installState.installStatus() == InstallStatus.CANCELED ||
                    installState.installStatus() == InstallStatus.UNKNOWN
            ) {
                handleAsyncSplashJustOnce();
            } else if (installState.installStatus() == InstallStatus.DOWNLOADED) {
                Toast.makeText(getApplicationContext(), getString(R.string.updated_and_ready_welcome_back), Toast.LENGTH_SHORT).show();
                appUpdateManager.completeUpdate();
            }
        };
    }

    private void handleAsyncSplashJustOnce() {
        if (!isHandleAsyncSplash) {
            AsyncSplash.Companion.getInstance().init(this, appOpenCallback, interCallback, "c193nrau3dhc", "", "ca-app-pub-3940256099942544~3347511713", jsonIdAdsDefault);
            //AsyncSplash.Companion.getInstance().setUseTechManager(); //case use TechManager Organic
            AsyncSplash.Companion.getInstance().setUseDetectTestAd(); //case use DetectTestAd
            //AsyncSplash.Companion.getInstance().setUseIdAdsFromRemoteConfig(true, "id_ads");
            AsyncSplash.Companion.getInstance().setDebug(true); //use for TechManager, DetectTestAd
//            AsyncSplash.Companion.getInstance().setLoadAndShowIdInterAdSplashAsync();
            AsyncSplash.Companion.getInstance().setPreloadResumeAds(false);
            //AsyncSplash.Companion.getInstance().setUseAdPreloading(true);
//            AsyncSplash.Companion.getInstance().setAsyncSplashAds();
            //AsyncSplash.Companion.getInstance().setLoopAdsSplash(true);
//            AsyncSplash.Companion.getInstance().setTimeOutSplash(12000);
            AsyncSplash.Companion.getInstance().setTimeOutCallApi(0);
            //AsyncSplash.Companion.getInstance().setUseIdAdsFromRemoteConfig("id_ads");
            //AsyncSplash.Companion.getInstance().setTimeOutCallIdRemoteConfig(5000);
//            IAPManager.getInstance().setPurchaseTest(true);
//            ArrayList<ProductDetailCustom> listIAP = new ArrayList<>();
//            listIAP.add(new ProductDetailCustom(IAPManager.PRODUCT_ID_TEST, IAPManager.typeSub, false));
//            listIAP.add(new ProductDetailCustom("coin_pack_100", IAPManager.typeIAP, true));
//            listIAP.add(new ProductDetailCustom("coin_pack_500", IAPManager.typeIAP, true));
//            AsyncSplash.Companion.getInstance().setUseBilling(listIAP); //if app use IAP
            AsyncSplash.Companion.getInstance().setInitResumeAdsNormal(); //init resume ads without welcome back
//            AsyncSplash.Companion.getInstance().setInitWelcomeBackBelowResumeAds(WelcomeBackActivity.class); //init resume ads with welcome back above
//            AsyncSplash.Companion.getInstance().setInitWelcomeBackBelowResumeAds(WelcomeBackActivity.class); //init resume ads with welcome back below
            ArrayList<String> listTurnOffRemote = new ArrayList<>();
            //listTurnOffRemote.add("native_wb");
            AsyncSplash.Companion.getInstance().setListKeyNativeAfterInterSplash("native_full_splash_2f", "native_full_splash");
            AsyncSplash.Companion.getInstance().setListKeyInterSplash("inter_splash_2f", "inter_splash");
            AsyncSplash.Companion.getInstance().setKeyNativeAfterInter("native_after_inter");
            AsyncSplash.Companion.getInstance().setListTurnOffRemoteKeys(listTurnOffRemote); //set list off remote of TechManager
//            ArrayList<String> listIdBannerSplash = new ArrayList<>();
//            listIdBannerSplash.add("ca-app-pub-3940256099942544/6300978111");
//            AsyncSplash.Companion.getInstance().setKeyAdsInterSplash("inter_splash");
//            AsyncSplash.Companion.getInstance().setKeyAdsOpenSplash("open_splash");
//            AsyncSplash.Companion.getInstance().setKeyAdsOpenResume("open_splash");
//            AsyncSplash.Companion.getInstance().setKeyIntervalBetweenInterstitial("interval_between_interstitial");
//            AsyncSplash.Companion.getInstance().setKeyIntervalInterstitialFromStart("interval_interstitial_from_start");
//            AsyncSplash.Companion.getInstance().setShowBannerSplash(binding.bannerContainerView, listIdBannerSplash, "banner_splash");
            AsyncSplash.Companion.getInstance().setOnPrepareLoadInterOpenSplashAds(new Function0<Unit>() {
                @Override
                public Unit invoke() { //prepare load and show inter/open splash
                    //RemoteConfigHelper.getInstance().set_config(SplashActivity.this, "inter_splash", false);
                    //RemoteConfigHelper.getInstance().set_config(SplashActivity.this, "open_splash", false);
                    RemoteConfigHelper.getInstance().set_config(SplashActivity.this, "native_full", false);
                    RemoteConfigHelper.getInstance().set_config(SplashActivity.this, "native_full_2f", true);
                    RemoteConfigHelper.getInstance().set_config(SplashActivity.this, "native_full_splash_2f", true);
                    RemoteConfigHelper.getInstance().set_config(SplashActivity.this, "native_full_splash", true);
                    RemoteConfigHelper.getInstance().set_config(SplashActivity.this, "inter_splash_2f", true);
                    RemoteConfigHelper.getInstance().set_config(SplashActivity.this, "inter_splash", true);
                    return null;
                }
            });
            AsyncSplash.Companion.getInstance().handleAsync(this, this,
                    LifecycleOwnerKt.getLifecycleScope(this), new Function0<Unit>() {
                        @Override
                        public Unit invoke() { //no internet
                            interCallback.onNextAction();
                            return null;
                        }
                    }, new Function0<Unit>() { //async splash done
                        @Override
                        public Unit invoke() {
                    /*ArrayList<Integer> listAnim = new ArrayList<>();
                    listAnim.add(R.raw.custom_loading);
                    Admob.getInstance().setCustomAnimationDialog(listAnim);
                    AppOpenManager.getInstance().setCustomAnimationDialog(listAnim);*/
                            //NativeAfterInterManager.preloadNativeAfterInter(SplashActivity.this, "native_all", "native_after_inter");
//                            InterManager.loadInterAdPreload(SplashActivity.this, "inter_all", "inter_all");

                            return null;
                        }
                    });
            isHandleAsyncSplash = true;
        }
    }

    private void startNextAct() {
        Log.d("SplashActivity", "startNextAct. " + AdmobApi.getInstance().getListIDByName("resume_wb").size());
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AsyncSplash.Companion.getInstance().checkShowSplashWhenFail();
        appUpdateManager.registerListener(installStateUpdatedListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        appUpdateManager.unregisterListener(installStateUpdatedListener);
    }

}
