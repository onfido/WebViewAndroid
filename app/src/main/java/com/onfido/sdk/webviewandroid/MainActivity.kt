package com.onfido.sdk.webviewandroid

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebView
import android.webkit.WebView.setWebContentsDebuggingEnabled
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.onfido.sdk.webviewandroid.ui.theme.AppTheme

private const val SDK_URL = "https://sdk.onfido.com"
private const val SDK_VERSION = "v14.49.0"

class MainActivity : ComponentActivity() {
    private val webViewState = mutableStateOf<WebView?>(null)

    private var fileChooserCallback: ValueCallback<Array<Uri>>? = null
    private val fileChooserLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        fileChooserCallback?.onReceiveValue(uris.toTypedArray())
        fileChooserCallback = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val permissionDelegate = ActivityPermissionDelegate(this)

        enableEdgeToEdge()
        setContent {
            AppTheme {
                WebViewScreen(
                    webViewState = webViewState,
                    permissionDelegate = permissionDelegate,
                    onFileChooser = { callback, _ ->
                        fileChooserCallback = callback
                        fileChooserLauncher.launch("*/*")
                    }
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        webViewState.value?.onPause()
    }

    override fun onResume() {
        super.onResume()
        webViewState.value?.onResume()
    }

    // Backwards navigation
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webViewState.value?.canGoBack() == true) {
            webViewState.value?.goBack()
            return true
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior
        return super.onKeyDown(keyCode, event)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun WebViewScreen(
    webViewState: MutableState<WebView?>,
    permissionDelegate: ActivityPermissionDelegate,
    onFileChooser: (ValueCallback<Array<Uri>>, Array<String>) -> Unit
) {
    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.domStorageEnabled = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.setSupportZoom(false)

            setWebContentsDebuggingEnabled(true)

            webViewClient = PermissionHandlingWebViewClient(permissionDelegate)
            webChromeClient = PermissionHandlingWebChromeClient(
                permissionDelegate = permissionDelegate,
                onFileChooser = onFileChooser,
            )

            addJavascriptInterface(JavaScriptInterface(), "sdk")
            loadDataWithBaseURL(
                SDK_URL,
                """
                    <html lang="en">
                    <head>
                        <title>IDV</title>
                        <style>
                            html, body {
                                margin: 0;
                                padding: 0;
                                height: 100%;
                                width: 100%;
                            }
                        </style>
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <script src="$SDK_URL/$SDK_VERSION/Onfido.iife.js"></script>
                    </head>
                    <body>
                    </body>
                    </html>
                    """.trimIndent(),
                "text/html",
                "UTF-8",
                null,
            )
        }
    }.apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )
        webViewState.value = this
    }

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars),
        factory = { webView },
    )
}

internal class JavaScriptInterface {
    @JavascriptInterface
    fun onComplete(data: String) {
        // Handle messages from onComplete, data is a json string
        Log.d("WebViewSampleApp", "onComplete: $data")
    }

    @JavascriptInterface
    fun onError(error: String) {
        // Handle messages from onError, error is a json string
        Log.d("WebViewSampleApp", "onError: $error")
    }
}
