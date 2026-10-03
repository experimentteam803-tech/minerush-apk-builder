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
import androidx.appcompat.app.AppCompatActivity;

// Unity Ads Official Imports
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsShowOptions;

public class MainActivity extends AppCompatActivity {

    private WebView webView;

    // =======================================================
    // 🎯 UNITY ADS REAL CONFIGURATION
    // =======================================================
    private static final String UNITY_GAME_ID = "800387446";
    private static final String REWARDED_PLACEMENT_ID = "BP_Rewarded_Android";
    private static final String INTERSTITIAL_PLACEMENT_ID = "BP_Interstitial_Android";
    private static final boolean TEST_MODE = true;

    private static final String APP_URL = "https://mine-rush-fawn.vercel.app/";
    private boolean isOffline = false;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Initialize Unity Ads
        initUnityAds();

        // 2. Setup WebView
        webView = findViewById(R.id.webview);
        webView.setBackgroundColor(Color.parseColor("#040711"));

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setMediaPlaybackRequiresUserGesture(false);

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

    private void initUnityAds() {
        UnityAds.initialize(this, UNITY_GAME_ID, TEST_MODE, new IUnityAdsInitializationListener() {
            @Override
            public void onInitializationComplete() {
                loadUnityAds();
            }

            @Override
            public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                // Retry if needed
            }
        });
    }

    private void loadUnityAds() {
        UnityAds.load(REWARDED_PLACEMENT_ID, new IUnityAdsLoadListener() {
            @Override
            public void onUnityAdsAdLoaded(String placementId) {}

            @Override
            public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {}
        });

        UnityAds.load(INTERSTITIAL_PLACEMENT_ID, new IUnityAdsLoadListener() {
            @Override
            public void onUnityAdsAdLoaded(String placementId) {}

            @Override
            public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {}
        });
    }

    // High-Tech Offline Screen
    private void showProfessionalOfflinePage() {
        String offlineHtml = "<!DOCTYPE html><html><head><meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0, user-scalable=no'>" +
                "<style>" +
                "* { margin:0; padding:0; box-sizing:border-box; font-family:-apple-system, sans-serif; }" +
                "body { background:#040711; color:#FFF; height:100vh; display:flex; flex-direction:column; align-items:center; justify-content:center; padding:24px; text-align:center; }" +
                ".radar-ring { width:96px; height:96px; border-radius:50%; background:rgba(0, 242, 254, 0.08); border:2px solid rgba(0, 242, 254, 0.3); display:flex; align-items:center; justify-content:center; font-size:42px; margin-bottom:20px; box-shadow:0 0 35px rgba(0, 242, 254, 0.2); }" +
                "h2 { font-size:22px; font-weight:800; margin-bottom:8px; }" +
                "h2 span { color:#00F2FE; }" +
                "p { font-size:13px; color:#94A3B8; max-width:280px; line-height:1.5; margin-bottom:28px; }" +
                ".btn-retry { background:linear-gradient(135deg, #00F2FE, #3B82F6); color:#040711; font-size:15px; font-weight:800; border:none; padding:15px 32px; border-radius:16px; cursor:pointer; }" +
                "</style></head><body>" +
                "<div class='radar-ring'>📡</div>" +
                "<h2>Connection <span>Lost</span></h2>" +
                "<p>Please check your mobile data or Wi-Fi network to resume cloud mining.</p>" +
                "<button class='btn-retry' id='retryBtn' onclick='handleRetry()'>⚡ Retry Connection</button>" +
                "<script>" +
                "function handleRetry() {" +
                "  if (window.AndroidBridge && window.AndroidBridge.retryConnection) {" +
                "    window.AndroidBridge.retryConnection();" +
                "  } else { window.location.href = '" + APP_URL + "'; }" +
                "}" +
                "window.addEventListener('online', function() { handleRetry(); });" +
                "</script></body></html>";

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

    // ================= JAVASCRIPT BRIDGE =================
    public class WebAppInterface {

        @JavascriptInterface
        public void retryConnection() {
            runOnUiThread(() -> {
                if (isNetworkAvailable()) {
                    webView.loadUrl(APP_URL);
                } else {
                    Toast.makeText(MainActivity.this, "Still offline! Turn on Mobile Data or Wi-Fi.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // 1. REWARDED AD (Mining ke liye)
        @JavascriptInterface
        public void showRewardedAd() {
            showUnityAdInternal("mining", REWARDED_PLACEMENT_ID);
        }

        // 2. REWARDED INTERSTITIAL AD (Tasks aur Coin Swap ke liye)
        @JavascriptInterface
        public void showRewardedInterstitialAd(String targetType) {
            showUnityAdInternal(targetType, REWARDED_PLACEMENT_ID);
        }

        // 3. REGULAR INTERSTITIAL AD
        @JavascriptInterface
        public void showInterstitialAd() {
            runOnUiThread(() -> {
                UnityAds.show(MainActivity.this, INTERSTITIAL_PLACEMENT_ID, new UnityAdsShowOptions(), null);
                loadUnityAds();
            });
        }

        private void showUnityAdInternal(String targetType, String placementId) {
            runOnUiThread(() -> {
                UnityAds.show(MainActivity.this, placementId, new UnityAdsShowOptions(), new IUnityAdsShowListener() {
                    @Override
                    public void onUnityAdsShowFailure(String pId, UnityAds.UnityAdsShowError error, String message) {
                        Toast.makeText(MainActivity.this, "Ad loading... Please tap again in 5s.", Toast.LENGTH_SHORT).show();
                        loadUnityAds();
                    }

                    @Override
                    public void onUnityAdsShowStart(String pId) {}

                    @Override
                    public void onUnityAdsShowClick(String pId) {}

                    @Override
                    public void onUnityAdsShowComplete(String pId, UnityAds.UnityAdsShowCompletionState state) {
                        if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                            webView.evaluateJavascript("window.onNativeAdRewarded('" + targetType + "');", null);
                        }
                        loadUnityAds();
                    }
                });
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
