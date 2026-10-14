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
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private static final String APP_URL = "https://mine-rush-fawn.vercel.app/";
    private boolean isOffline = false;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Setup WebView
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

        // 2. Intent Redirection Handler (External links open in phone browser)
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                return handleUrlRedirect(url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUrlRedirect(url);
            }

            private boolean handleUrlRedirect(String url) {
                if (url == null) return false;

                if (url.startsWith(APP_URL) || url.startsWith("https://mine-rush-fawn.vercel.app")) {
                    return false;
                }

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
                    try {
                        Intent fallback = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(fallback);
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
    // 🌐 JAVASCRIPT BRIDGE INTERFACE (CLEAN & ERROR-FREE)
    // =========================================================================
    public class WebAppInterface {

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
        // 🛡️ 100% ACCURATE HARDWARE-LEVEL ANDROID VPN DETECTOR
        @JavascriptInterface
        public boolean isVpnConnected() {
            try {
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if (cm != null) {
                    android.net.Network activeNetwork = cm.getActiveNetwork();
                    if (activeNetwork != null) {
                        android.net.NetworkCapabilities caps = cm.getNetworkCapabilities(activeNetwork);
                        if (caps != null) {
                            return caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_VPN);
                        }
                    }
                }
            } catch (Exception ignored) {}
            return false;
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

        // Bridge placeholders so Web App never throws an error
        @JavascriptInterface
        public void showRewardedAd(String tag) {}

        @JavascriptInterface
        public void showRewardedInterstitialAd(String tag) {}

        @JavascriptInterface
        public void showInterstitialAd() {}
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
