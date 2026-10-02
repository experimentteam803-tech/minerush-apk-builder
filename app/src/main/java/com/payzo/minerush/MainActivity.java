package com.payzo.minerush;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private RewardedAd rewardedAd;
    private RewardedInterstitialAd rewardedInterstitialAd;
    private InterstitialAd interstitialAd;

    // Google AdMob Test IDs
    private static final String ID_REWARDED = "ca-app-pub-3940256099942544/5224354917";
    private static final String ID_REWARDED_INTERSTITIAL = "ca-app-pub-3827595599617180/7024643071";
    private static final String ID_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712";

    private static final String APP_URL = "https://mine-rush-fawn.vercel.app/";
    private boolean isOffline = false;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, initializationStatus -> {});
        loadAllAds();

        webView = findViewById(R.id.webview);
        
        // 🛡️ Fix 1: White screen freeze khatam karne ke liye dark background
        webView.setBackgroundColor(Color.parseColor("#040711"));

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        webView.addJavascriptInterface(new WebAppInterface(), "AndroidBridge");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                isOffline = false;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    isOffline = true;
                    showProfessionalOfflinePage();
                }
            }
        });

        webView.loadUrl(APP_URL);
    }

    private void loadAllAds() {
        loadRewardedAd();
        loadRewardedInterstitialAd();
        loadInterstitialAd();
    }

    private void loadRewardedAd() {
        AdRequest req = new AdRequest.Builder().build();
        RewardedAd.load(this, ID_REWARDED, req, new RewardedAdLoadCallback() {
            @Override public void onAdLoaded(@NonNull RewardedAd ad) { rewardedAd = ad; }
            @Override public void onAdFailedToLoad(@NonNull LoadAdError err) { rewardedAd = null; }
        });
    }

    private void loadRewardedInterstitialAd() {
        AdRequest req = new AdRequest.Builder().build();
        RewardedInterstitialAd.load(this, ID_REWARDED_INTERSTITIAL, req, new RewardedInterstitialAdLoadCallback() {
            @Override public void onAdLoaded(@NonNull RewardedInterstitialAd ad) { rewardedInterstitialAd = ad; }
            @Override public void onAdFailedToLoad(@NonNull LoadAdError err) { rewardedInterstitialAd = null; }
        });
    }

    private void loadInterstitialAd() {
        AdRequest req = new AdRequest.Builder().build();
        InterstitialAd.load(this, ID_INTERSTITIAL, req, new InterstitialAdLoadCallback() {
            @Override public void onAdLoaded(@NonNull InterstitialAd ad) { interstitialAd = ad; }
            @Override public void onAdFailedToLoad(@NonNull LoadAdError err) { interstitialAd = null; }
        });
    }

    // 📡 Ultra-Sleek Offline Page (No-White-Screen Freeze)
    private void showProfessionalOfflinePage() {
        String offlineHtml = "<!DOCTYPE html><html><head><meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0, user-scalable=no'>" +
                "<style>" +
                "* { margin:0; padding:0; box-sizing:border-box; font-family:-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }" +
                "body { background:#040711; color:#FFF; height:100vh; display:flex; flex-direction:column; align-items:center; justify-content:center; padding:24px; text-align:center; overflow:hidden; }" +
                ".radar-ring { width:96px; height:96px; border-radius:50%; background:rgba(0, 242, 254, 0.08); border:2px solid rgba(0, 242, 254, 0.3); display:flex; align-items:center; justify-content:center; font-size:42px; margin-bottom:20px; box-shadow:0 0 35px rgba(0, 242, 254, 0.2); animation:pulse 2s infinite; }" +
                "@keyframes pulse { 0% { transform:scale(0.96); box-shadow:0 0 15px rgba(0,242,254,0.2); } 50% { transform:scale(1.04); box-shadow:0 0 35px rgba(0,242,254,0.4); } 100% { transform:scale(0.96); box-shadow:0 0 15px rgba(0,242,254,0.2); } }" +
                "h2 { font-size:22px; font-weight:800; margin-bottom:8px; color:#FFF; }" +
                "h2 span { color:#00F2FE; }" +
                "p { font-size:13px; color:#94A3B8; max-width:280px; line-height:1.5; margin-bottom:28px; }" +
                ".btn-retry { background:linear-gradient(135deg, #00F2FE, #3B82F6); color:#040711; font-size:15px; font-weight:800; border:none; padding:15px 32px; border-radius:16px; cursor:pointer; box-shadow:0 4px 22px rgba(0, 242, 254, 0.35); text-transform:uppercase; letter-spacing:0.5px; transition:transform 0.15s; }" +
                ".btn-retry:active { transform:scale(0.96); }" +
                "</style></head><body>" +
                "<div class='radar-ring'>📡</div>" +
                "<h2>Connection <span>Lost</span></h2>" +
                "<p>Please check your mobile data or Wi-Fi network to resume cloud mining.</p>" +
                "<button class='btn-retry' id='retryBtn' onclick='handleRetry()'>⚡ Retry Connection</button>" +
                "<script>" +
                "function handleRetry() {" +
                "  var btn = document.getElementById('retryBtn');" +
                "  btn.innerText = 'Connecting...';" +
                "  btn.style.opacity = '0.6';" +
                "  if (window.AndroidBridge && window.AndroidBridge.retryConnection) {" +
                "    window.AndroidBridge.retryConnection();" +
                "  } else {" +
                "    window.location.href = '" + APP_URL + "';" +
                "  }" +
                "  setTimeout(function() { btn.innerText = '⚡ Retry Connection'; btn.style.opacity = '1'; }, 3000);" +
                "}" +
                "window.addEventListener('online', function() { handleRetry(); });" +
                "</script></body></html>";

        // 🛡️ Fix 2: Base URL me APP_URL use kiya hai taaki blank document na bane
        webView.loadDataWithBaseURL(APP_URL, offlineHtml, "text/html", "UTF-8", null);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnected();
        }
        return false;
    }

    public class WebAppInterface {

        // 🛡️ Fix 3: Smart Native Retry Connection
        @JavascriptInterface
        public void retryConnection() {
            runOnUiThread(() -> {
                if (isNetworkAvailable()) {
                    webView.loadUrl(APP_URL);
                } else {
                    Toast.makeText(MainActivity.this, "Still offline! Please turn on Mobile Data or Wi-Fi.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @JavascriptInterface
        public void showRewardedAd() {
            runOnUiThread(() -> {
                if (rewardedAd != null) {
                    rewardedAd.show(MainActivity.this, rewardItem -> {
                        webView.evaluateJavascript("window.onNativeAdRewarded('mining');", null);
                        loadRewardedAd();
                    });
                } else {
                    Toast.makeText(MainActivity.this, "Rewarded Ad loading... Please wait 5 seconds.", Toast.LENGTH_SHORT).show();
                    loadRewardedAd();
                }
            });
        }

        @JavascriptInterface
        public void showRewardedInterstitialAd(String targetType) {
            runOnUiThread(() -> {
                if (rewardedInterstitialAd != null) {
                    rewardedInterstitialAd.show(MainActivity.this, rewardItem -> {
                        webView.evaluateJavascript("window.onNativeAdRewarded('" + targetType + "');", null);
                        loadRewardedInterstitialAd();
                    });
                } else {
                    Toast.makeText(MainActivity.this, "Rewarded Interstitial loading... Please wait 5 seconds.", Toast.LENGTH_SHORT).show();
                    loadRewardedInterstitialAd();
                }
            });
        }

        @JavascriptInterface
        public void showInterstitialAd() {
            runOnUiThread(() -> {
                if (interstitialAd != null) {
                    interstitialAd.show(MainActivity.this);
                    loadInterstitialAd();
                } else {
                    loadInterstitialAd();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (isOffline) {
            super.onBackPressed();
            return;
        }
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
