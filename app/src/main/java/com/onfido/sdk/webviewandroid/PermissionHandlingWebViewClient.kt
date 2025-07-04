package com.onfido.sdk.webviewandroid

import android.Manifest.permission.CAMERA
import android.Manifest.permission.MODIFY_AUDIO_SETTINGS
import android.Manifest.permission.RECORD_AUDIO
import android.webkit.WebView
import android.webkit.WebViewClient

internal class PermissionHandlingWebViewClient(
    private val permissionDelegate: ActivityPermissionDelegate,
) : WebViewClient() {

    override fun onPageFinished(
        view: WebView?,
        url: String?,
    ) {
        super.onPageFinished(view, url)

        view?.let {
            val video = permissionDelegate.hasPermission(CAMERA)
            val audio = permissionDelegate.hasPermission(RECORD_AUDIO)
                    && permissionDelegate.hasPermission(MODIFY_AUDIO_SETTINGS)
            if (video || audio) {
                // Camera permission has been granted to the app already, and by
                // invoking navigator.mediaDevices.getUserMedia, we'll trigger the
                // onPermissionRequest callback, which can bring the permission to the webview
                it.evaluateJavascript(
                    "navigator.mediaDevices.getUserMedia({video: ${jsBool(video)}, audio: ${
                        jsBool(
                            audio
                        )
                    }})",
                    null,
                )
            }

            // Add your SDK token here
            val token = "SDK_TOKEN"

            it.evaluateJavascript(
                """
                    Onfido.init({
                        token: '$token',
                        steps: ['welcome', 'document'],
                        containerEl: document.body,
                        onComplete: (data) => sdk.onComplete(JSON.stringify(data)),
                        onError: (error) => sdk.onError(JSON.stringify(error))
                    })
                """.trimIndent(),
                null,
            )
        }
    }

    private fun jsBool(value: Boolean): String =
        if (value) {
            "true"
        } else {
            "false"
        }
}
