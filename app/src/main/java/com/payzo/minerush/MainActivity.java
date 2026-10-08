package com.payzo.minerush;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// Start.io Official SDK Imports
import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;
import com.startapp.sdk.adsbase.adlisteners.VideoListener;

public class MainActivity extends AppCompatActivity {

    private WebView webView;

    // Aapki Start.io App ID
    private static final String STARTIO_APP_ID = "209581916";

    private StartAppAd startAppRewardedAd;
    private StartAppAd startAppInterstitialAd;
    private String currentRewardTarget = "mining";

    // 🛡️ ANTI-DOUBLE REWARD LOCK
    private boolean isRewardAlreadyDelivered = false;

    private static final String APP_URL = "https://mine-rush-fawn.vercel.app/";
    private boolean isOffline = false;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Initialize Start.io Engine (Privacy Dialog OFF & Return Ads OFF)
        StartAppSDK.init(this, STARTIO_APP_ID, false);
        StartAppSDK.enableReturnAds(false);
        // Privacy Pop-up ko band karna
        StartAppSDK.setUserConsent(this, "pas", System.currentTimeMillis(), false);

        startAppRewardedAd = new StartAppAd(this);
        startAppInterstitialAd = new StartAppAd(this);

        preloadStartIoAds();

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
        settings.setSupportMultipleWindows(true);

        webView.addJavascriptInterface(new WebAppInterface(), "AndroidBridge");

        // 3. SMART EXTERNAL INTENT & AD REDIRECTION HANDLER (Play Store / Chrome Support)
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                return handleUrlNavigation(url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUrlNavigation(url);
            }

            private boolean handleUrlNavigation(String url) {
                if (url == null) return false;

                // Agar url apne MineRush web app ka hai, toh WebView me hi chalne do
                if (url.startsWith(APP_URL) || url.startsWith("https://mine-rush-fawn.vercel.app")) {
                    return false;
                }

                // Agar ad ya external link hai (Google Play Store, market://, ya external website)
                try {
                    Intent intent;
                    if (url.startsWith("market://") || url.startsWith("intent://")) {
                        intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    } else {
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    }
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    return true;
                } catch (Exception e) {
                    // Agar app phone me na ho (jaise specific market), toh browser me khol do
                    try {
                        Intent browserFallback = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(browserFallback);
                        return true;
                    } catch (Exception ignored) {}
                }
                return false;
            }

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

    private void preloadStartIoAds() {
        if (startAppRewardedAd != null) {
            startAppRewardedAd.loadAd(StartAppAd.AdMode.REWARDED_VIDEO);
        }
        if (startAppInterstitialAd != null) {
            startAppInterstitialAd.loadAd(StartAppAd.AdMode.AUTOMATIC);
        }
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

    // =========================================================================
    // 🌐 JAVASCRIPT BRIDGE INTERFACE
    // =========================================================================
    public class WebAppInterface {

        // In-App Background APK Download
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

        // 1. REWARDED ADS (Mining, Upgrade, Swap)
        @JavascriptInterface
        public void showRewardedInterstitialAd(String targetType) {
            playSmartRewardedAd(targetType != null ? targetType : "mining");
        }

        // 2. REWARDED ADS (Task 1)
        @JavascriptInterface
        public void showRewardedAd() {
            playSmartRewardedAd("admob_task1");
        }

        @JavascriptInterface
        public void showRewardedAd(String targetType) {
            playSmartRewardedAd(targetType != null ? targetType : "mining");
        }

        // 3. REWARDED ADS (Task 2 & 7-Day Claim)
        @JavascriptInterface
        public void showAppLovinRewarded(String targetType) {
            playSmartRewardedAd(targetType != null ? targetType : "task_2");
        }

        // 4. AUTO INTERSTITIAL
        @JavascriptInterface
        public void showAppLovinInterstitial() {
            showStartIoInterstitial();
        }

        @JavascriptInterface
        public void showInterstitialAd() {
            showStartIoInterstitial();
        }
    }

    // =========================================================================
    // ⚡ SMART SINGLE-REWARD ENGINE
    // =========================================================================
    private void playSmartRewardedAd(final String targetType) {
        currentRewardTarget = targetType;
        isRewardAlreadyDelivered = false;

        runOnUiThread(() -> {
            if (startAppRewardedAd == null) {
                startAppRewardedAd = new StartAppAd(MainActivity.this);
            }

            startAppRewardedAd.setVideoListener(new VideoListener() {
                @Override
                public void onVideoCompleted() {
                    deliverSingleReward(currentRewardTarget);
                }
            });

            boolean displayed = startAppRewardedAd.showAd(new AdDisplayListener() {
                @Override public void adHidden(Ad ad) {
                    deliverSingleReward(currentRewardTarget);
                    preloadStartIoAds();
                }
                @Override public void adDisplayed(Ad ad) {}
                @Override public void adClicked(Ad ad) {}
                @Override public void adNotDisplayed(Ad ad) {
                    showFallbackInterstitialReward(currentRewardTarget);
                }
            });

            if (!displayed) {
                showFallbackInterstitialReward(currentRewardTarget);
            }
        });
    }

    private void showFallbackInterstitialReward(final String targetType) {
        if (startAppInterstitialAd == null) {
            startAppInterstitialAd = new StartAppAd(MainActivity.this);
        }

        boolean displayed = startAppInterstitialAd.showAd(new AdDisplayListener() {
            @Override
            public void adHidden(Ad ad) {
                deliverSingleReward(targetType);
                preloadStartIoAds();
            }
            @Override public void adDisplayed(Ad ad) {}
            @Override public void adClicked(Ad ad) {}
            @Override public void adNotDisplayed(Ad ad) {
                startAppInterstitialAd.loadAd(StartAppAd.AdMode.AUTOMATIC, new AdEventListener() {
                    @Override
                    public void onReceiveAd(Ad ad) {
                        startAppInterstitialAd.showAd(new AdDisplayListener() {
                            @Override public void adHidden(Ad ad) {
                                deliverSingleReward(targetType);
                                preloadStartIoAds();
                            }
                            @Override public void adDisplayed(Ad ad) {}
                            @Override public void adClicked(Ad ad) {}
                            @Override public void adNotDisplayed(Ad ad) {
                                deliverSingleReward(targetType);
                            }
                        });
                    }

                    @Override
                    public void onFailedToReceiveAd(Ad ad) {
                        Toast.makeText(MainActivity.this, "Network busy. Reward granted!", Toast.LENGTH_SHORT).show();
                        deliverSingleReward(targetType);
                    }
                });
            }
        });

        if (!displayed) {
            preloadStartIoAds();
        }
    }

    private synchronized void deliverSingleReward(String tag) {
        if (isRewardAlreadyDelivered) return;
        isRewardAlreadyDelivered = true;

        new Handler(Looper.getMainLooper()).post(() -> {
            if (webView != null) {
                webView.evaluateJavascript("if(window.onNativeAdRewarded){ window.onNativeAdRewarded('" + tag + "'); }", null);
            }
        });
    }

    private void showStartIoInterstitial() {
        runOnUiThread(() -> {
            if (startAppInterstitialAd == null) {
                startAppInterstitialAd = new StartAppAd(MainActivity.this);
            }

            boolean displayed = startAppInterstitialAd.showAd(new AdDisplayListener() {
                @Override public void adHidden(Ad ad) { preloadStartIoAds(); }
                @Override public void adDisplayed(Ad ad) {}
                @Override public void adClicked(Ad ad) {}
                @Override public void adNotDisplayed(Ad ad) { preloadStartIoAds(); }
            });

            if (!displayed) {
                startAppInterstitialAd.loadAd(StartAppAd.AdMode.AUTOMATIC);
            }
        });
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
