package com.pollitoproductions.bionicbiome;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
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
 * v105 — NUCLEAR DIAGNOSTIC BUILD
 *
 * Stripped to absolute minimum to diagnose why ad touches don't work.
 * NO immersive mode. NO fullscreen. NO window flag manipulation.
 * Status bar and nav bar WILL be visible (ugly but diagnostic).
 * WebView set to GONE during ad so it can't intercept touches.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BionicBiome";
    private WebView webView;
    private Vibrator vibrator;
    private RewardedAd rewardedAd;
    private boolean isLoadingAd = false;
    private volatile boolean isShowingAd = false;
    private static final String GAME_URL = "https://pollitoproductions.github.io/bionic-biome/";
    private static final String REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        MobileAds.initialize(this, initializationStatus -> {
            Log.d(TAG, "AdMob SDK initialized");
            loadRewardedAd();
        });

        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        setupWebView();
        webView.loadUrl(GAME_URL);
    }

    // =========================================================================
    // AdMob
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
                            Log.d(TAG, "Ad showing");
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            Log.d(TAG, "Ad dismissed");
                            runOnUiThread(() -> {
                                rewardedAd = null;
                                isShowingAd = false;
                                if (webView != null) {
                                    webView.setVisibility(View.VISIBLE);
                                }
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
                                loadRewardedAd();
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
            loadRewardedAd();
            return;
        }

        isShowingAd = true;

        // Hide WebView so it CANNOT intercept any touches
        if (webView != null) {
            webView.setVisibility(View.GONE);
        }

        final RewardedAd adToShow = rewardedAd;
        Log.d(TAG, "Showing ad");
        adToShow.show(this, rewardItem -> {
            Log.d(TAG, "Reward earned");
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
    // WebView
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

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(new VibrationInterface(), "AndroidVibration");
        webView.addJavascriptInterface(new AndroidInterface(), "Android");
    }

    // =========================================================================
    // Lifecycle — NO immersive mode anywhere
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
        if (webView != null) {
            webView.onResume();
            webView.resumeTimers();
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
    // JS interfaces
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
