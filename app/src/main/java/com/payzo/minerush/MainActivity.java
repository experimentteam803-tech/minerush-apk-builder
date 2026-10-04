package com.payzo.minerush;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
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

// Google AdMob Imports
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;

// AppLovin Imports
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;
import com.applovin.adview.AppLovinIncentivizedInterstitial;
import com.applovin.adview.AppLovinInterstitialAd;
import com.applovin.adview.AppLovinInterstitialAdDialog;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdDisplayListener;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdRewardListener;
import com.applovin.sdk.AppLovinAdVideoPlaybackListener;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private WebView webView;

    // =========================================================================
    // 🎯 1. GOOGLE ADMOB TEST AD UNIT IDs (Baad me yahan Real IDs lagana)
    // =========================================================================
    // Mining, Rig Upgrade & Currency Swap ke liye
    private static final String ADMOB_TEST_REWARDED_INTERSTITIAL = "ca-app-pub-3940256099942544/5354046379";
    // Task 1 ke liye
    private static final String ADMOB_TEST_REWARDED = "ca-app-pub-3940256099942544/5224354917";

    private RewardedInterstitialAd admobRewardedInterstitial;
    private RewardedAd admobRewarded;

    // =========================================================================
    // 🎯 2. APPLOVIN TEST CONFIGURATION (Task 2, 7-Day Claim, Auto Interstitial)
    // =========================================================================
    private AppLovinIncentivizedInterstitial appLovinRewarded;
    private AppLovinInterstitialAdDialog appLovinInterstitial;

    private static final String APP_URL = "https://mine-rush-fawn.vercel.app/";
    private boolean isOffline = false;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Initialize AdMob Engine
        MobileAds.initialize(this, initializationStatus -> {});
        loadAdMobAds();

        // 2. Initialize AppLovin Engine in Test Mode
        AppLovinSdk appLovinSdk = AppLovinSdk.getInstance(this);
        appLovinSdk.getSettings().setVerboseLogging(true);
        appLovinSdk.initializeSdk((AppLovinSdkConfiguration configuration) -> {
            loadAppLovinAds();
        });

        // 3. Setup WebView
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

    // ================= ADS LOADING =================
    private void loadAdMobAds() {
        // Load AdMob Rewarded Interstitial
        AdRequest req1 = new AdRequest.Builder().build();
        RewardedInterstitialAd.load(this, ADMOB_TEST_REWARDED_INTERSTITIAL, req1, new RewardedInterstitialAdLoadCallback() {
            @Override public void onAdLoaded(@NonNull RewardedInterstitialAd ad) { admobRewardedInterstitial = ad; }
            @Override public void onAdFailedToLoad(@NonNull LoadAdError err) { admobRewardedInterstitial = null; }
        });

        // Load AdMob Standard Rewarded Video
        AdRequest req2 = new AdRequest.Builder().build();
        RewardedAd.load(this, ADMOB_TEST_REWARDED, req2, new RewardedAdLoadCallback() {
            @Override public void onAdLoaded(@NonNull RewardedAd ad) { admobRewarded = ad; }
            @Override public void onAdFailedToLoad(@NonNull LoadAdError err) { admobRewarded = null; }
        });
    }

    private void loadAppLovinAds() {
        appLovinRewarded = AppLovinIncentivizedInterstitial.create(this);
        appLovinRewarded.preload(new AppLovinAdLoadListener() {
            @Override public void adReceived(AppLovinAd ad) {}
            @Override public void failedToReceiveAd(int errorCode) {}
        });

        appLovinInterstitial = AppLovinInterstitialAd.create(AppLovinSdk.getInstance(this), this);
    }

    // High-Tech Offline Screen
    private void showProfessionalOfflinePage() {
        String offlineHtml = "<!DOCTYPE html><html><head><meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0, user-scalable=no'>" +
                "<style>" +
                "* { margin:0; padding:0; box-sizing:border-box; font-family:-apple-system, sans-serif; }" +
                "body { background:#040711; color:#FFF; height:100vh; display:flex; flex-direction:column; align-items:center; justify-content:center; padding:24px; text-align:center; overflow:hidden; }" +
                ".radar-ring { width:96px; height:96px; border-radius:50%; background:rgba(0, 242, 254, 0.08); border:2px solid rgba(0, 242, 254, 0.3); display:flex; align-items:center; justify-content:center; font-size:42px; margin-bottom:20px; box-shadow:0 0 35px rgba(0, 242, 254, 0.2); animation:pulse 2s infinite; }" +
                "@keyframes pulse { 0% { transform:scale(0.96); box-shadow:0 0 15px rgba(0,242,254,0.2); } 50% { transform:scale(1.04); box-shadow:0 0 35px rgba(0,242,254,0.4); } 100% { transform:scale(0.96); box-shadow:0 0 15px rgba(0,242,254,0.2); } }" +
                "h2 { font-size:22px; font-weight:800; margin-bottom:8px; color:#FFF; }" +
                "h2 span { color:#00F2FE; }" +
                "p { font-size:13px; color:#94A3B8; max-width:280px; line-height:1.5; margin-bottom:28px; }" +
                ".btn-retry { background:linear-gradient(135deg, #00F2FE, #3B82F6); color:#040711; font-size:15px; font-weight:800; border:none; padding:15px 32px; border-radius:16px; cursor:pointer; box-shadow:0 4px 22px rgba(0, 242, 254, 0.35); text-transform:uppercase; letter-spacing:0.5px; }" +
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
        // 🚀 IN-APP BACKGROUND DOWNLOAD & AUTO INSTALL
        @JavascriptInterface
        public void downloadAndInstallApk(String downloadUrl) {
            runOnUiThread(() -> {
                try {
                    Toast.makeText(MainActivity.this, "Downloading update in background...", Toast.LENGTH_LONG).show();

                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
                    request.setTitle("MineRush Update");
                    request.setDescription("Downloading latest version...");
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "MineRush-Update.apk");
                    request.setMimeType("application/vnd.android.package-archive");

                    DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                    if (dm != null) {
                        long downloadId = dm.enqueue(request);

                        // Download complete hote hi automatically Install screen kholo
                        registerReceiver(new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                                if (id == downloadId) {
                                    Uri apkUri = dm.getUriForDownloadedFile(downloadId);
                                    if (apkUri != null) {
                                        Intent installIntent = new Intent(Intent.ACTION_VIEW);
                                        installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                                        installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(installIntent);
                                    }
                                }
                            }
                        }, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                    }
                } catch (Exception e) {
                    // Agar koi dikkat aaye toh external browser me link open karein
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl));
                    startActivity(browserIntent);
                }
            });
        }

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

        // 1. ADMOB REWARDED INTERSTITIAL (Mining, Upgrade, Swap)
        @JavascriptInterface
        public void showRewardedInterstitialAd(String targetType) {
            runOnUiThread(() -> {
                if (admobRewardedInterstitial != null) {
                    admobRewardedInterstitial.show(MainActivity.this, rewardItem -> {
                        webView.evaluateJavascript("window.onNativeAdRewarded('" + targetType + "');", null);
                        loadAdMobAds();
                    });
                } else {
                    Toast.makeText(MainActivity.this, "AdMob loading... Tap again in 5s.", Toast.LENGTH_SHORT).show();
                    loadAdMobAds();
                }
            });
        }

        // 2. ADMOB STANDARD REWARDED (Task 1)
        @JavascriptInterface
        public void showRewardedAd() {
            runOnUiThread(() -> {
                if (admobRewarded != null) {
                    admobRewarded.show(MainActivity.this, rewardItem -> {
                        webView.evaluateJavascript("window.onNativeAdRewarded('admob_task1');", null);
                        loadAdMobAds();
                    });
                } else {
                    Toast.makeText(MainActivity.this, "AdMob video loading... Tap again in 5s.", Toast.LENGTH_SHORT).show();
                    loadAdMobAds();
                }
            });
        }

        // 3. APPLOVIN REWARDED (Task 2 & 7-Day Daily Claim)
        @JavascriptInterface
        public void showAppLovinRewarded(String targetType) {
            runOnUiThread(() -> {
                if (appLovinRewarded != null && appLovinRewarded.isAdReadyToDisplay()) {
                    appLovinRewarded.show(MainActivity.this, new AppLovinAdRewardListener() {
                        @Override
                        public void userRewardVerified(AppLovinAd ad, Map<String, String> response) {
                            webView.evaluateJavascript("window.onNativeAdRewarded('" + targetType + "');", null);
                            loadAppLovinAds();
                        }
                        @Override public void userOverQuota(AppLovinAd ad, Map<String, String> response) {}
                        @Override public void userRewardRejected(AppLovinAd ad, Map<String, String> response) {}
                        @Override public void validationRequestFailed(AppLovinAd ad, int errorCode) {}
                    }, new AppLovinAdVideoPlaybackListener() {
                        @Override public void videoPlaybackBegan(AppLovinAd ad) {}
                        @Override public void videoPlaybackEnded(AppLovinAd ad, double percentViewed, boolean fullyWatched) {}
                    }, new AppLovinAdDisplayListener() {
                        @Override public void adDisplayed(AppLovinAd ad) {}
                        @Override public void adHidden(AppLovinAd ad) { loadAppLovinAds(); }
                    }, null);
                } else {
                    Toast.makeText(MainActivity.this, "AppLovin ad loading... Tap again in 5s.", Toast.LENGTH_SHORT).show();
                    loadAppLovinAds();
                }
            });
        }

        // 4. APPLOVIN AUTOMATIC INTERSTITIAL (Natural Tab Browsing)
        @JavascriptInterface
        public void showAppLovinInterstitial() {
            runOnUiThread(() -> {
                if (appLovinInterstitial != null) {
                    appLovinInterstitial.show();
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
