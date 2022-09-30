package com.example.webviewandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WebView myWebView;
        String URL = "https://crowd-testing.eu.onfido.app/f/da5ed749-9d93-4026-bf43-4e96c02e5f15/31617a19-85aa-418b-9bce-ccf092232500";
        myWebView = (WebView) findViewById(R.id.webview);
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.getSettings().setLoadsImagesAutomatically(true);
        myWebView.getSettings().setAppCacheEnabled(true); // Disable while debugging
        myWebView.setWebViewClient(new WebViewClient()); // avoid opening in browser
        myWebView.loadUrl(URL);
    }
}