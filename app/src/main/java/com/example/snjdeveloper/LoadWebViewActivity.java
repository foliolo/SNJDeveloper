package com.example.snjdeveloper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.snjdeveloper.admin.HomeAdmin;

import java.util.Objects;

public class LoadWebViewActivity extends AppCompatActivity {
    private View progressbar;
    private View errorScreen;
    private WebView webView;
    private boolean containsLink = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_webview);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        try {
            setTitle("");
            toolbar.getNavigationIcon()
                    .setColorFilter(ContextCompat.getColor(this, R.color.black), PorterDuff.Mode.SRC_ATOP);
            Intent intent = getIntent();
            loadWebView(Objects.requireNonNull(intent.getStringExtra("HTML")),
                    intent.getStringExtra("TITLE"));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error occurred", Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadWebView(String html, String title) {
        webView = findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        if (!html.startsWith("https://")) {
            containsLink = false;
            String encode = Base64.encodeToString(html.getBytes(), Base64.NO_PADDING);
            webView.loadData(encode, "text/html", "base64");
        } else {
            webView.getSettings().setLoadsImagesAutomatically(true);
            webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
            progressbar = findViewById(R.id.progressbar);
            progressbar.setVisibility(View.VISIBLE);
            errorScreen = findViewById(R.id.errorScreen);
            webView.setWebViewClient(new Browse());
            webView.setWebChromeClient(new BrowseChromeClient(this));
            webView.loadUrl(html);
        }
        if (title != null) {
            setTitle(title);

        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        backOnClicked(new View(this));
        return true;
    }

    @Override
    public void onBackPressed() {
        backOnClicked(new View(this));
    }

    public void backOnClicked(View view) {
        if (containsLink && webView.canGoBack())
            webView.goBack();
        else
            back();
    }

    void back() {
        if (isTaskRoot()) {
            startActivity(new Intent(this, HomeAdmin.class));
            finish();
        } else
            super.onBackPressed();
    }

    public void reloadWebView(View view) {
        webView.reload();
        errorScreen.setVisibility(View.GONE);
        progressbar.setVisibility(View.VISIBLE);
    }

    private class Browse extends WebViewClient {
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (error.getErrorCode() != 1) {
                    errorScreen.setVisibility(View.VISIBLE);
                    progressbar.setVisibility(View.GONE);
                } else onBackPressed();
            } else {
                errorScreen.setVisibility(View.VISIBLE);
                progressbar.setVisibility(View.GONE);
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (progressbar != null)
                progressbar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (progressbar != null)
                progressbar.setVisibility(View.GONE);
            String title = view.getTitle();
            if (!title.toLowerCase().contains("not available") && !title.toLowerCase().contains("not found"))
                setTitle(title);
            //else errorScreen.setVisibility(View.VISIBLE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
//             view.loadUrl(url);
            return super.shouldOverrideUrlLoading(view, request);
        }
    }

    private class BrowseChromeClient extends WebChromeClient {
        Context context;

        public BrowseChromeClient(Context context) {
            this.context = context;
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            try {
                new AlertDialog.Builder(context)
                        .setTitle("Confirmation")
                        .setMessage(message)
                        .setPositiveButton("OK", (dialog, which) -> result.confirm())
//                        .setNegativeButton("Cancel", (dialog, which) -> result.cancel())
                        .create()
                        .show();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
    }
}
