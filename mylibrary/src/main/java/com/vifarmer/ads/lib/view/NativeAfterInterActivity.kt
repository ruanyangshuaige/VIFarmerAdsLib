package com.vifarmer.ads.lib.view

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.vifarmer.ads.lib.callback.InterCallback
import com.vifarmer.ads.lib.ads.native_ads.NativeAfterInterManager
import com.vifarmer.ads.lib.R

class NativeAfterInterActivity : AppCompatActivity() {
    private lateinit var frAds: FrameLayout

    companion object {
        var interCallback: InterCallback? = null
        var adsKey: String = "native_after_inter"
        var remoteKey: String = "native_after_inter"
        var timeDelayShowXButton: Int = 3000
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        window.hideNavigation()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_native_after_inter)

        frAds = findViewById(R.id.fr_ads)
        Log.d("Admob", "Native After Inter: Show Screen Native After Inter");

    }

    override fun onResume() {
        super.onResume()
        NativeAfterInterManager.showPreloadNativeAfterInter(
            timeDelayShowXButton,
            frAds,
            this,
            adsKey,
            remoteKey,
            object : NativeAfterInterManager.OnCloseNativeListener {
                override fun onClose() {
                    Log.d("Admob", "Native After Inter: Show Screen Native After Inter");
                    interCallback?.onNextAction()
                    finish()
                }

                override fun onFail() {
                    Log.d("Admob", "Native After Inter: Show Screen Native After Inter");
                    interCallback?.onNextAction()
                    finish()
                }
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        adsKey = ""
        remoteKey = ""
    }


    fun Window.hideNavigation() {
        if (setFullScreenWallpaper()) return

        decorView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            val activityRoot = decorView
            activityRoot.getWindowVisibleDisplayFrame(rect)
            if (setFullScreenWallpaper()) return@addOnGlobalLayoutListener
        }
    }

    private fun Window.setFullScreenWallpaper(): Boolean {
        val windowInsetsController: WindowInsetsControllerCompat? =
            if (Build.VERSION.SDK_INT >= 30) {
                ViewCompat.getWindowInsetsController(decorView)
            } else {
                WindowInsetsControllerCompat(this, decorView)
            }

        if (windowInsetsController == null) {
            return true
        }
        setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        )
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
        windowInsetsController.hide(WindowInsetsCompat.Type.systemGestures())
        return false
    }
}