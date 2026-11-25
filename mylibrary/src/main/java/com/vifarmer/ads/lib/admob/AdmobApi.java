package com.vifarmer.ads.lib.admob;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.vifarmer.ads.lib.Utils.NetworkUtil;
import com.vifarmer.ads.lib.ads.app_open_ads.AppOpenManager;
import com.vifarmer.ads.lib.call_api.AdsModel;
import com.vifarmer.ads.lib.call_api.ApiService;
import com.vifarmer.ads.lib.callback.ApiCallback;
import com.vifarmer.ads.lib.callback.AppOpenCallback;
import com.vifarmer.ads.lib.callback.InterCallback;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AdmobApi {
    private String TAG = "AdmobApi";
    private ApiService apiService;
    private String linkServer = "https://language-master.top";
    private String packageName = "";
    public String appIDRelease = "ca-app-pub-4973559944609228~2346710863";
    private static volatile AdmobApi INSTANCE;
    private Context context;
    private String jsonIdAdsDefault = "";
    private boolean isSetId = false;
    private int timeOutCallApi = 12000;
    private final Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private Runnable timeoutRunnable;

    public int getListAdsSize() {
        if (listAds != null) {
            return listAds.size();
        } else {
            return 0;
        }
    }

    public String getJsonIdAdsDefault() {
        return jsonIdAdsDefault;
    }

    public void setJsonIdAdsDefault(String jsonIdAdsDefault) {
        this.jsonIdAdsDefault = jsonIdAdsDefault;
    }

    public int getTimeOutCallApi() {
        return timeOutCallApi;
    }

    public void setTimeOutCallApi(int timeOutCallApi) {
        this.timeOutCallApi = timeOutCallApi;
    }

    LinkedHashMap<String, List<String>> listAds = new LinkedHashMap<>();

    public List<String> getListIDOpenSplash() {
        return getListIDByName("open_splash");
    }

    public List<String> getListIDNativeLanguage() {
        return getListIDByName("native_language");
    }

    public List<String> getListIDNativeIntro() {
        return getListIDByName("native_intro");
    }

    public List<String> getListIDNativePermission() {
        return getListIDByName("native_permission");
    }

    public List<String> getListIDNativeAll() {
        return getListIDByName("native_all");
    }

    public List<String> getListIDInterSplash() {
        return getListIDByName("inter_splash");
    }

    public List<String> getListIDInterAll() {
        return getListIDByName("inter_all");
    }

    public List<String> getListIDBannerAll() {
        return getListIDByName("banner_all");
    }

    public List<String> getListIDCollapseBannerAll() {
        return getListIDByName("collapse_banner");
    }

    public List<String> getListIDInterIntro() {
        return getListIDByName("inter_intro");
    }

    public List<String> getListIDAppOpenResume() {
        return getListIDByName("open_resume");
    }

    public List<String> getListIDByName(String nameAds) {
        List<String> list = new ArrayList<>();
        if (listAds.get(nameAds.trim()) != null)
            list.addAll(Objects.requireNonNull(listAds.get(nameAds)));
        return list;
    }

    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    public static synchronized AdmobApi getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AdmobApi();
        }
        return INSTANCE;
    }

    public void init(Context context, String linkServerRelease, String AppID, ApiCallback callBack) {
        this.context = context;
        listAds.clear();
        isSetId = false;
        cancelTimeout();
        this.packageName = context.getPackageName();
        if (linkServerRelease != null && AppID != null) {
            String trimmedServer = linkServerRelease.trim();
            if (!trimmedServer.isEmpty()
                    && (trimmedServer.startsWith("http://")
                    || trimmedServer.startsWith("https://"))) {
                while (trimmedServer.endsWith("/")) {
                    trimmedServer = trimmedServer.substring(0, trimmedServer.length() - 1);
                }
                this.linkServer = trimmedServer;
                this.appIDRelease = AppID.trim();
            }
        }

        String baseURL = linkServer + "/api/";
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(timeOutCallApi, TimeUnit.MILLISECONDS)
                .readTimeout(timeOutCallApi, TimeUnit.MILLISECONDS)
                .writeTimeout(timeOutCallApi, TimeUnit.MILLISECONDS)
                .build();
        apiService = new Retrofit.Builder()
                .baseUrl(baseURL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(ApiService.class);

        Log.i(TAG, "link Server:" + baseURL);

        if (NetworkUtil.isNetworkActive(context)) {
            fetchData(callBack);
            //after timeout, if cannot call api -> set list id default
            timeoutRunnable = () -> {
                if (!isSetId) { //if not set id from api -> set list id default
                    fallbackToDefault(callBack, "timeout");
                } else {
                    Log.d(TAG, "xxxxxx1");
                }
            };
            timeoutHandler.postDelayed(timeoutRunnable, timeOutCallApi);
        } else {
            fallbackToDefault(callBack, "no_network");
        }
    }

    private void cancelTimeout() {
        if (timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
            timeoutRunnable = null;
        }
    }

    private void fallbackToDefault(ApiCallback callBack, String reason) {
        cancelTimeout();
        if (!isSetId) {
            convertJsonIdAdsDefaultToList(jsonIdAdsDefault);
            isSetId = true;
            Log.d(TAG, "convertJsonIdAdsDefaultToList: " + reason + ": isSetId = true");
            if (callBack != null) {
                callBack.onReady();
            }
        }
    }

    public void convertJsonIdAdsDefaultToList(String jsonIdAds) {
        listAds.clear();
        if (jsonIdAds == null || jsonIdAds.trim().isEmpty()) {
            Log.w(TAG, "convertJsonIdAdsDefaultToList: jsonIdAdsDefault is empty, skipping default parse");
            return;
        }
        try {
            JSONArray jsonArray = new JSONArray(jsonIdAds);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String name = jsonObject.getString("name").trim();
                String ads_id = jsonObject.getString("ads_id");

                // check if 'name' exists
                List<String> listIDAds = listAds.get(name);
                if (listIDAds == null) {
                    listIDAds = new ArrayList<>();
                    listAds.put(name, listIDAds);
                }

                // add ads_id
                listIDAds.add(ads_id);
            }
            Log.d(TAG, "convertJsonIdAdsDefaultToList: " + listAds.size());

            for (Map.Entry<String, List<String>> entry : listAds.entrySet()) {
                Log.d(TAG, "Key: " + entry.getKey() + ", Value: " + entry.getValue());
            }
        } catch (Exception e) {
            Log.e(TAG, "convertJsonIdAdsDefaultToList: Exception: " + e.getMessage());
        }
    }

    private void fetchData(ApiCallback callBack) {
        Log.d(TAG, "fetchData: start");
        try {
            String appID_package = appIDRelease + "+" + packageName;
            Log.i(TAG, "link Server query :" + linkServer + "/api/getidv2/" + appID_package);
            apiService.callAds(appID_package).enqueue(new Callback<List<AdsModel>>() {
                @Override
                public void onResponse(@NonNull Call<List<AdsModel>> call, @NonNull Response<List<AdsModel>> response) {
                    cancelTimeout();
                    Log.d(TAG, "onResponse: code=" + response.code() + ", isSetId=" + isSetId);
                    if (!isSetId) {
                        List<AdsModel> body = response.body();
                        if (!response.isSuccessful() || body == null || body.isEmpty()) {
                            Log.w(TAG, "onResponse: unsuccessful or empty body, code=" + response.code());
                            fallbackToDefault(callBack, "empty_or_error_response");
                            return;
                        }
                        Log.d(TAG, "onResponse: " + listAds.size());
                        for (AdsModel ads : body) {
                            if (ads == null || ads.getName() == null || ads.getAds_id() == null) {
                                continue;
                            }
                            String name = ads.getName().trim();
                            if (name.isEmpty()) {
                                continue;
                            }
                            List<String> listIDAds = listAds.get(name);
                            if (listIDAds == null) {
                                listIDAds = new ArrayList<>();
                                listAds.put(name, listIDAds);
                            }
                            listIDAds.add(ads.getAds_id());
                            Log.d(TAG, name + "_" + ads.getAds_id());
                        }
                        isSetId = true;
                        Log.d(TAG, "isSetId = true2, listAds size = " + listAds.size());
                        if (callBack != null) {
                            callBack.onReady();
                        }
                    } else {
                        Log.d(TAG, "xxxxxx2");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<AdsModel>> call, @NonNull Throwable t) {
                    Log.e(TAG, "onFailure: " + t.getMessage(), t);
                    Log.d(TAG, "onFailure: isSetId: " + isSetId);
                    if (!isSetId) {
                        fallbackToDefault(callBack, "onFailure");
                    } else {
                        Log.d(TAG, "xxxxxx3");
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "fetchData: Exception: isSetId: " + isSetId, e);
            if (!isSetId) {
                fallbackToDefault(callBack, "Exception");
            } else {
                Log.d(TAG, "xxxxxx4");
            }
        }
    }

    public void loadOpenAppAdSplashFloor(AppCompatActivity activity, String adsKey, AppOpenCallback appOpenCallback) {
        AppOpenManager.getInstance().loadAndShowAppOpenResumeSplash(activity, AdmobApi.getInstance().getListIDByName(adsKey), appOpenCallback);
    }

    public void loadInterAdSplashFloor(AppCompatActivity activity, String adsKey, InterCallback interCallback) {
        Admob.getInstance().loadAndShowInterAdSplash(activity, AdmobApi.getInstance().getListIDByName(adsKey), interCallback);
    }

    public void loadInterAdSplashFloorDelayAds(AppCompatActivity activity, String adsKey, InterCallback interCallback, String adsKeyNative, String remoteKeyNative) {
        Admob.getInstance().loadAndShowInterAdSplashDelay(activity, AdmobApi.getInstance().getListIDByName(adsKey), interCallback, adsKeyNative, remoteKeyNative);
    }

    public void loadAndShowIdInterAdSplashAsync(AppCompatActivity activity, String adsKey, InterCallback interCallback) {
        Admob.getInstance().loadAndShowIdInterAdSplashAsync(activity, AdmobApi.getInstance().getListIDByName(adsKey), interCallback);
    }

    public void loadOpenAppAdSplashLoop(AppCompatActivity activity, String adsKey, AppOpenCallback appOpenCallback) {
        AppOpenManager.getInstance().loadAndShowAppOpenResumeSplashLoop(activity, AdmobApi.getInstance().getListIDByName(adsKey), appOpenCallback);
    }

    public void loadInterAdSplashLoop(AppCompatActivity activity, String adsKey, InterCallback interCallback) {
        Admob.getInstance().loadAndShowInterAdSplashLoop(activity, AdmobApi.getInstance().getListIDByName(adsKey), interCallback);
    }

    public void loadAndShowInterAdPreloadingSplash(AppCompatActivity activity, String adsKey, InterCallback interCallback, String adsKeyNative, String remoteKeyNative){
        Admob.getInstance().loadAndShowInterAdPreloadingSplashDelay(activity, AdmobApi.getInstance().getListIDByName(adsKey), interCallback, adsKeyNative, remoteKeyNative);
    }

    public void loadAndShowAppOpenAdPreloadingSplash(AppCompatActivity activity, String adsKey, AppOpenCallback appOpenCallback){
        AppOpenManager.getInstance().loadAndShowAdPreloadingAppOpenSplash(activity,AdmobApi.getInstance().getListIDByName(adsKey), appOpenCallback);
    }
}
