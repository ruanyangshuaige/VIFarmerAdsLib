package com.vifarmer.ads.lib.ads.inter_ads;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.vifarmer.ads.lib.ads.collapse_banner_ads.CollapseBannerHelper;

import java.util.ArrayList;

public class InterHelper {
    private static final String TAG = "InterHelper";

    public static class CloseButtonInfo {
        public View view;
        public int id;
        public String idString;

        public CloseButtonInfo(View view, int id, String idString) {
            this.view = view;
            this.id = id;
            this.idString = idString;
        }
    }

    public static CloseButtonInfo findCloseButtonView(ViewGroup root) {
        if (root == null) return null;
        int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = root.getChildAt(i);
            int viewId = child.getId();
            String viewIdString = "No ID";
            try {
                if (viewId != View.NO_ID && root.getResources() != null) {
                    viewIdString = root.getResources().getResourceEntryName(viewId);
                }
            } catch (Exception ignored) {}

            String className = child.getClass().getName();
            CharSequence contentDesc = child.getContentDescription();
            String contentDescStr = contentDesc != null ? contentDesc.toString() : "";

            Log.d(TAG, "findCloseButtonView checking: " + className + ", ID: " + viewIdString + ", ContentDesc: " + contentDescStr);

            String lowerId = viewIdString.toLowerCase();
            String lowerDesc = contentDescStr.toLowerCase();

            if (lowerId.contains("close") || lowerId.contains("btn_close") || lowerId.contains("x") ||
                    lowerDesc.contains("close") || lowerDesc.contains("đóng")) {
                Log.d(TAG, "Found Interstitial Close Button View! Class: " + className + ", ID: " + viewIdString + " (" + viewId + ")");
                return new CloseButtonInfo(child, viewId, viewIdString);
            }

            if (child instanceof ViewGroup) {
                CloseButtonInfo info = findCloseButtonView((ViewGroup) child);
                if (info != null) {
                    return info;
                }
            }
        }
        return null;
    }

    public static CloseButtonInfo findCloseButtonInActivity(Activity activity) {
        if (activity == null) return null;

        // First check activity's decorView
        if (activity.getWindow() != null && activity.getWindow().getDecorView() instanceof ViewGroup) {
            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            CloseButtonInfo info = findCloseButtonView(decorView);
            if (info != null) return info;
        }

        // Check WindowManager views if available
        ArrayList<Object> wmViews = CollapseBannerHelper.getWindowManagerViews();
        if (wmViews != null) {
            for (Object viewObj : wmViews) {
                if (viewObj instanceof ViewGroup) {
                    CloseButtonInfo info = findCloseButtonView((ViewGroup) viewObj);
                    if (info != null) return info;
                }
            }
        }

        return null;
    }

    public static void attachLottieToCloseButton(Activity activity, int lottieRawRes) {
        if (activity == null) return;

        CloseButtonInfo closeInfo = findCloseButtonInActivity(activity);
        if (closeInfo == null || closeInfo.view == null) {
            Log.d(TAG, "attachLottieToCloseButton: Close button view not found");
            return;
        }

        try {
            View closeView = closeInfo.view;
            int resId = lottieRawRes;
            if (resId == 0) {
                resId = activity.getResources().getIdentifier("hand_focus", "raw", activity.getPackageName());
            }

            LottieAnimationView lottieView = new LottieAnimationView(activity);
            if (resId != 0) {
                lottieView.setAnimation(resId);
            }
            lottieView.setRepeatCount(LottieDrawable.INFINITE);
            lottieView.playAnimation();
            lottieView.setClickable(false);
            lottieView.setFocusable(false);

            if (closeView.getParent() instanceof ViewGroup) {
                ViewGroup parent = (ViewGroup) closeView.getParent();
                ViewGroup.LayoutParams lp = closeView.getLayoutParams();

                int width = closeView.getWidth();
                int height = closeView.getHeight();
                float density = activity.getResources().getDisplayMetrics().density;

                // Scale Lottie view up
                lottieView.setScaleX(1.4f);
                lottieView.setScaleY(1.4f);

                // Shift Lottie view down close to the bottom edge of closeView and slightly to the right
                float shiftX = (width > 0) ? (width * 0.3f) : (10 * density);
                float shiftY = (height > 0) ? (height * 0.4f) : (14 * density);

                lottieView.setTranslationX(shiftX);
                lottieView.setTranslationY(shiftY);

                parent.addView(lottieView, lp);
                Log.d(TAG, "Successfully attached LottieAnimationView to Interstitial Close button parent!");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error attaching Lottie to close button: " + e.getMessage(), e);
        }
    }
}
