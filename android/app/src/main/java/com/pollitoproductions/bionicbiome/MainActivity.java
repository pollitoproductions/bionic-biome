package com.pollitoproductions.bionicbiome;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BionicBiome";
    private WebView webView;
    private Vibrator vibrator;
    private RewardedAd rewardedAd;
    private boolean isLoadingAd = false;
    private volatile boolean isShowingAd = false;
    private static final String GAME_URL = "https://pollitoproductions.github.io/bionic-biome/";

    // Google AdMob Test Rewarded Ad Unit ID
    private static final String REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        MobileAds.initialize(this, initializationStatus -> {
            Log.d(TAG, "AdMob SDK initialized");
            loadRewardedAd();
        });

        // Limit display refresh rate to 60Hz for consistent game speed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
            layoutParams.preferredDisplayModeId = findBest60HzMode();
            getWindow().setAttributes(layoutParams);
        }

        enableImmersiveMode();

        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        setupWebView();
        webView.loadUrl(GAME_URL);
    }

    // =========================================================================
    // Immersive mode — ONLY used for the game, NEVER touched around ads
    // =========================================================================

    private void enableImmersiveMode() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(
                    android.view.WindowInsets.Type.statusBars()
                    | android.view.WindowInsets.Type.navigationBars()
                );
                controller.setSystemBarsBehavior(
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
            );
        }
    }

    // =========================================================================
    // AdMob Rewarded Ad
    // =========================================================================

    private void loadRewardedAd() {
        if (isLoadingAd || rewardedAd != null) {
            return;
        }
        isLoadingAd = true;
        Log.d(TAG, "Loading rewarded ad...");

        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, REWARDED_AD_UNIT_ID, adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd ad) {
                rewardedAd = ad;
                isLoadingAd = false;
                Log.d(TAG, "Rewarded ad loaded successfully");

                rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent() {
                        Log.d(TAG, "Rewarded ad is now showing full screen");
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        Log.d(TAG, "Rewarded ad dismissed");
                        runOnUiThread(() -> {
                            rewardedAd = null;
                            isShowingAd = false;
                            // Re-apply immersive mode now that the ad is gone
                            enableImmersiveMode();
                            loadRewardedAd();
                        });
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        Log.e(TAG, "Rewarded ad failed to show: " + adError.getMessage());
                        runOnUiThread(() -> {
                            rewardedAd = null;
                            isShowingAd = false;
                            enableImmersiveMode();
                            loadRewardedAd();
                            if (webView != null) {
                                webView.evaluateJavascript(
                                    "(function(){" +
                                    "  var btn=document.getElementById('revive-ad-btn');" +
                                    "  if(btn){btn.classList.remove('disabled');btn.textContent='\\u25B6 WATCH AD TO CONTINUE';}" +
                                    "})()", null);
                            }
                        });
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                rewardedAd = null;
                isLoadingAd = false;
                Log.e(TAG, "Rewarded ad failed to load: " + loadAdError.getMessage());
            }
        });
    }

    /**
     * Show the rewarded ad.
     *
     * v103 FIX: Two independent fixes for the un-closable ad:
     *
     * 1) ProGuard rules now keep com.google.android.gms.internal.ads.**
     *    (the SDK's rendering + touch classes that R8 was stripping).
     *
     * 2) Before calling show(), we explicitly SHOW the system bars and
     *    reset the behavior to BEHAVIOR_DEFAULT.  This eliminates the
     *    invisible gesture-interception zones that IMMERSIVE_STICKY /
     *    BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE create at screen edges.
     *    The ad's X button sits in the top-right where the status-bar
     *    gesture zone is — the first tap there was being swallowed by
     *    the system to transiently reveal the bar, never reaching the
     *    ad's close button.
     *
     *    We post show() one frame later so the bar state is settled.
     */
    private void showReviveAd() {
        if (isShowingAd) {
            Log.w(TAG, "showReviveAd: already showing, ignoring");
            return;
        }

        if (rewardedAd == null) {
            Log.w(TAG, "Rewarded ad not loaded, attempting reload");
            runOnUiThread(() -> {
                if (webView != null) {
                    webView.evaluateJavascript(
                        "(function(){" +
                        "  var btn=document.getElementById('revive-ad-btn');" +
                        "  if(btn){btn.classList.remove('disabled');btn.textContent='AD NOT READY';}" +
                        "  setTimeout(function(){if(btn){btn.textContent='\\u25B6 WATCH AD TO CONTINUE';}},2000);" +
                        "})()", null);
                }
            });
            loadRewardedAd();
            return;
        }

        isShowingAd = true;

        // ---- Clear immersive mode so the ad's touch targets work ----
        // This removes the gesture-interception zones at screen edges
        // that swallow the first tap (preventing the X button from working).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.show(
                    android.view.WindowInsets.Type.statusBars()
                    | android.view.WindowInsets.Type.navigationBars()
                );
                controller.setSystemBarsBehavior(
                    WindowInsetsController.BEHAVIOR_DEFAULT
                );
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_VISIBLE
            );
        }

        // ---- Show the ad on the next frame so bar state has settled ----
        final RewardedAd adToShow = rewardedAd;
        getWindow().getDecorView().post(() -> {
            Log.d(TAG, "Showing rewarded ad NOW");
            if (adToShow != null && !isFinishing()) {
                adToShow.show(MainActivity.this, rewardItem -> {
                    Log.d(TAG, "User earned reward: " + rewardItem.getAmount()
                        + " " + rewardItem.getType());
                    runOnUiThread(() -> {
                        if (webView != null) {
                            webView.evaluateJavascript(
                                "if(window.revivePlayer){ window.revivePlayer(); }",
                                null
                            );
                        }
                    });
                });
            }
        });
    }

    // =========================================================================
    // Display helpers
    // =========================================================================

    private int findBest60HzMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            WindowManager windowManager = getWindowManager();
            Display display = windowManager.getDefaultDisplay();
            Display.Mode[] modes = display.getSupportedModes();

            int bestMode = 0;
            float targetRefreshRate = 60.0f;
            float smallestDifference = Float.MAX_VALUE;

            for (Display.Mode mode : modes) {
                float refreshRate = mode.getRefreshRate();
                float difference = Math.abs(refreshRate - targetRefreshRate);
                if (difference < smallestDifference) {
                    smallestDifference = difference;
                    bestMode = mode.getModeId();
                }
            }
            return bestMode;
        }
        return 0;
    }

    // =========================================================================
    // WebView setup
    // =========================================================================

    private void setupWebView() {
        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                injectFrameRateLimiter();
            }
        });

        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(new VibrationInterface(), "AndroidVibration");
        webView.addJavascriptInterface(new AndroidInterface(), "Android");
    }

    private void injectFrameRateLimiter() {
        String javascript =
            "(function() {" +
            "    const targetFPS = 60;" +
            "    const frameDuration = 1000 / targetFPS;" +
            "    let lastFrameTime = performance.now();" +
            "    const originalRAF = window.requestAnimationFrame;" +
            "    window.requestAnimationFrame = function(callback) {" +
            "        return originalRAF(function(currentTime) {" +
            "            const elapsed = currentTime - lastFrameTime;" +
            "            if (elapsed >= frameDuration - 1) {" +
            "                lastFrameTime = currentTime - (elapsed % frameDuration);" +
            "                callback(currentTime);" +
            "            } else {" +
            "                window.requestAnimationFrame(callback);" +
            "            }" +
            "        });" +
            "    };" +
            "})();";

        webView.evaluateJavascript(javascript, null);
    }

    // =========================================================================
    // Activity lifecycle
    // =========================================================================

    @Override
    public void onBackPressed() {
        // While the ad is showing, let the framework handle back press
        // so the ad SDK can dismiss if appropriate.
        if (isShowingAd) {
            super.onBackPressed();
            return;
        }
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) {
            webView.onPause();
            webView.pauseTimers();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Only re-enable immersive mode when NOT showing an ad
        if (!isShowingAd) {
            enableImmersiveMode();
        }
        if (webView != null) {
            webView.onResume();
            webView.resumeTimers();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        // Only re-enable immersive mode when NOT showing an ad
        if (hasFocus && !isShowingAd) {
            enableImmersiveMode();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.loadUrl("about:blank");
            webView.stopLoading();
            webView.setWebViewClient(null);
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

    // =========================================================================
    // JavaScript interfaces
    // =========================================================================

    private class VibrationInterface {
        @JavascriptInterface
        public void vibrate(int duration) {
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(duration);
                }
            }
        }

        @JavascriptInterface
        public void vibratePattern(String pattern) {
            if (vibrator != null && vibrator.hasVibrator()) {
                try {
                    String[] parts = pattern.split(",");
                    long[] timings = new long[parts.length];
                    for (int i = 0; i < parts.length; i++) {
                        timings[i] = Long.parseLong(parts[i].trim());
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createWaveform(timings, -1));
                    } else {
                        vibrator.vibrate(timings, -1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class AndroidInterface {
        @JavascriptInterface
        public void onGameOver() {
            Log.d(TAG, "onGameOver() called from JavaScript");
            runOnUiThread(() -> showReviveAd());
        }

        @JavascriptInterface
        public boolean isAdReady() {
            return rewardedAd != null;
        }
    }
}
