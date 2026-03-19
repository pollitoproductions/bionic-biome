package com.pollitoproductions.bionicbiome;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.ads.MobileAds;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MobileAds.initialize(this, initializationStatus -> {});
        setContentView(R.layout.activity_main);

        WebView webView = findViewById(R.id.webview);
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        // Replace this string in `strings.xml` with your deployed URL
        String url = getString(R.string.start_url);
        webView.loadUrl(url);
    }
}
