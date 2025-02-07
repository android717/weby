package com.ravix.weby;

/**
 * Created by Ravix
 * Date: 06-02-2025
 * Time: 03:48 pm
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class weby extends AppCompatActivity {

    private static final String EXTRA_URL = "extra_url";
    private WebView webView;
    private static final String TAG = " Web";
    private ActivityResultLauncher<String[]> requestPermissionsLauncher;

    public static void start(Context context, String url) {
        Intent intent = new Intent(context, weby.class);
        intent.putExtra(EXTRA_URL, url);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        initWeb();

        requestPermissionsLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            boolean cameraPermissionGranted = result.getOrDefault(android.Manifest.permission.CAMERA, false);
            boolean audioPermissionGranted = result.getOrDefault(android.Manifest.permission.RECORD_AUDIO, false);

            if (cameraPermissionGranted && audioPermissionGranted) {
                webView.reload();
            } else {
                showPermissionsDeniedDialog();
            }
        });


        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView != null && webView.canGoBack()) {
                    webView.goBack();
                } else {
                    finish();
                }
            }
        });

    }

    private void initWeb() {
        webView = findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        String webUrl = getIntent().getStringExtra(EXTRA_URL);

        webSettings.setJavaScriptEnabled(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri url = request.getUrl();

                if (url.toString().startsWith("intent://")) {
                    try {
                        Intent intent = Intent.parseUri(url.toString(), Intent.URI_INTENT_SCHEME);
                        if (intent != null) {
                            startActivity(intent);
                            return true;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error handling intent-based URL", e);
                    }
                }

                if (url.getScheme() != null && (
                        url.getScheme().equalsIgnoreCase("whatsapp") ||
                                url.getScheme().equalsIgnoreCase("mailto") ||
                                url.getScheme().equalsIgnoreCase("tel"))) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, url);
                        startActivity(intent);
                        return true;
                    } catch (Exception e) {
                        Log.e(TAG, "Error handling app URL", e);
                    }
                }

                return false;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                handlePermissionRequest(request);
            }

            @Override
            public void onPermissionRequestCanceled(PermissionRequest request) {
                // Handle the case when the user cancels the permission request
                Log.i(TAG, "Permission request canceled by the user");
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                // Update progress bar or any other UI element
                Log.i(TAG, "Page loading progress: " + newProgress + "%");
            }

        });

        if (webUrl != null) {
            webView.loadUrl(webUrl);
        }
    }

    private void handlePermissionRequest(final PermissionRequest request) {
        boolean cameraPermissionGranted = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean audioPermissionGranted = ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

        if (!cameraPermissionGranted || !audioPermissionGranted) {
            showPermissionDialog(request);
        } else {
            request.grant(request.getResources());
        }
    }

    private void showPermissionDialog(PermissionRequest request) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Permissions Required")
                .setMessage("This application requires camera and audio permissions.")
                .setPositiveButton("Grant", (dialog, which) -> requestPermissionsLauncher.launch(new String[]{
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.RECORD_AUDIO
                }))
                .setNegativeButton("Cancel", (dialog, which) -> {
                    if (request != null) {
                        request.deny();
                    }
                })
                .show();
    }

    private void showPermissionsDeniedDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Permissions Denied")
                .setMessage("Without the required permissions, the app cannot access the camera and audio.")
                .setNeutralButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) {
            webView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
        }
    }
}
