package com.pollitoproductions.bionicbiome;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
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

/**
 * v106 — Production build with real AdMob ads.
 * Immersive mode restored. Real ad unit IDs.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BionicBiome";
    private WebView webView;
    private Vibrator vibrator;
    private RewardedAd rewardedAd;
    private boolean isLoadingAd = false;
    private volatile boolean isShowingAd = false;
    private static final String GAME_URL = "https://pollitoproductions.github.io/bionic-biome/";

    // Real AdMob Rewarded Ad Unit ID
    private static final String REWARDED_AD_UNIT_ID = "ca-app-pub-7858482153655813/5665829293";

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
    // Immersive mode
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
        if (isLoadingAd || rewardedAd != null) return;
        isLoadingAd = true;
        Log.d(TAG, "Loading rewarded ad...");

        RewardedAd.load(this, REWARDED_AD_UNIT_ID, new AdRequest.Builder().build(),
            new RewardedAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull RewardedAd ad) {
                    rewardedAd = ad;
                    isLoadingAd = false;
                    Log.d(TAG, "Rewarded ad loaded");

                    rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdShowedFullScreenContent() {
                            Log.d(TAG, "Rewarded ad showing");
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            Log.d(TAG, "Rewarded ad dismissed");
                            runOnUiThread(() -> {
                                rewardedAd = null;
                                isShowingAd = false;
                                if (webView != null) {
                                    webView.setVisibility(View.VISIBLE);
                                }
                                enableImmersiveMode();
                                loadRewardedAd();
                            });
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            Log.e(TAG, "Ad failed to show: " + adError.getMessage());
                            runOnUiThread(() -> {
                                rewardedAd = null;
                                isShowingAd = false;
                                if (webView != null) {
                                    webView.setVisibility(View.VISIBLE);
                                }
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
                    Log.e(TAG, "Ad failed to load: " + loadAdError.getMessage());
                }
            });
    }

    private void showReviveAd() {
        if (isShowingAd) return;
        if (rewardedAd == null) {
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

        // Hide WebView so it cannot intercept touches meant for the ad
        if (webView != null) {
            webView.setVisibility(View.GONE);
        }

        final RewardedAd adToShow = rewardedAd;
        Log.d(TAG, "Showing rewarded ad");
        adToShow.show(this, rewardItem -> {
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
            Log.d(TAG, "onGameOver() from JS");
            runOnUiThread(() -> showReviveAd());
        }

        @JavascriptInterface
        public boolean isAdReady() {
            return rewardedAd != null;
        }
    }
}
