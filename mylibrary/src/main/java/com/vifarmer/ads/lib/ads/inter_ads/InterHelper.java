package com.vifarmer.ads.lib.ads.inter_ads;

import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

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

            boolean isCloseId = lowerId.contains("close") || lowerId.contains("dismiss") ||
                    lowerId.equals("x") || lowerId.equals("btn_x") || lowerId.endsWith("_x") || lowerId.startsWith("x_");
            boolean isCloseDesc = lowerDesc.contains("close") || lowerDesc.contains("đóng") ||
                    lowerDesc.contains("dismiss") || lowerDesc.trim().equals("x");

            if (isCloseId || isCloseDesc) {
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

        // Check WindowManager views first if available (where AdMob dialogs/popups usually reside)
        ArrayList<Object> wmViews = CollapseBannerHelper.getWindowManagerViews();
        if (wmViews != null) {
            for (Object viewObj : wmViews) {
                if (viewObj instanceof ViewGroup) {
                    CloseButtonInfo info = findCloseButtonView((ViewGroup) viewObj);
                    if (info != null) return info;
                }
            }
        }

        // Check activity's decorView
        if (activity.getWindow() != null && activity.getWindow().getDecorView() instanceof ViewGroup) {
            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            CloseButtonInfo info = findCloseButtonView(decorView);
            if (info != null) return info;
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

        View closeView = closeInfo.view;
        closeView.post(() -> {
            try {
                if (!(closeView.getParent() instanceof ViewGroup)) return;

                ViewGroup parent = (ViewGroup) closeView.getParent();

                // Avoid duplicate attachments
                if (parent.findViewWithTag("LOTTIE_INTER_CLOSE") != null) {
                    Log.d(TAG, "LottieAnimationView already attached.");
                    return;
                }

                int resId = (lottieRawRes != 0) ? lottieRawRes : com.vifarmer.ads.lib.R.raw.hand_focus;

                LottieAnimationView lottieView = new LottieAnimationView(activity);
                lottieView.setTag("LOTTIE_INTER_CLOSE");
                lottieView.setAnimation(resId);
                lottieView.setRepeatCount(LottieDrawable.INFINITE);
                lottieView.setClickable(false);
                lottieView.setFocusable(false);

                // Disable clipping on parent and grandparent so translated lottieView won't be cut off
                parent.setClipChildren(false);
                parent.setClipToPadding(false);
                if (parent.getParent() instanceof ViewGroup) {
                    ((ViewGroup) parent.getParent()).setClipChildren(false);
                    ((ViewGroup) parent.getParent()).setClipToPadding(false);
                }

                int width = closeView.getWidth();
                int height = closeView.getHeight();
                float density = activity.getResources().getDisplayMetrics().density;

                int w = (width > 0) ? width : (int) (36 * density);
                int h = (height > 0) ? height : (int) (36 * density);

                // Tăng kích thước cơ sở của LottieView lớn hơn (ít nhất 72dp hoặc gấp 2.2 lần nút Close)
                int lottieW = Math.max((int) (w * 2.2f), (int) (72 * density));
                int lottieH = Math.max((int) (h * 2.2f), (int) (72 * density));

                ViewGroup.LayoutParams lp;
                if (parent instanceof FrameLayout) {
                    FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(lottieW, lottieH);
                    flp.gravity = Gravity.TOP | Gravity.END;
                    lp = flp;
                } else if (parent instanceof RelativeLayout) {
                    RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(lottieW, lottieH);
                    ViewGroup.LayoutParams closeLp = closeView.getLayoutParams();
                    if (closeLp instanceof RelativeLayout.LayoutParams) {
                        rlp = new RelativeLayout.LayoutParams((RelativeLayout.LayoutParams) closeLp);
                        rlp.width = lottieW;
                        rlp.height = lottieH;
                    }
                    lp = rlp;
                } else {
                    lp = new ViewGroup.LayoutParams(lottieW, lottieH);
                }

                // Scale Lottie view lên thêm 1.3 lần để bàn tay thật to và rõ ràng
                lottieView.setScaleX(1.3f);
                lottieView.setScaleY(1.3f);

                // Định vị sao cho đầu ngón tay chỉ thẳng vào tâm nút icClose
                float shiftX = (w * 0.15f);
                float shiftY = (h * 0.25f);

                lottieView.setTranslationX(shiftX);
                lottieView.setTranslationY(shiftY);

                lottieView.setElevation(closeView.getElevation() + 10f);

                parent.addView(lottieView, lp);
                lottieView.bringToFront();
                lottieView.playAnimation();

                Log.d(TAG, "Successfully attached LottieAnimationView to Interstitial Close button parent!");
            } catch (Exception e) {
                Log.e(TAG, "Error attaching Lottie to close button: " + e.getMessage(), e);
            }
        });
    }
}
