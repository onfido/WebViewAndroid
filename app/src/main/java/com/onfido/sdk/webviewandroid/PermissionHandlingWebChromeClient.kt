package com.onfido.sdk.webviewandroid

import android.Manifest.permission.CAMERA
import android.Manifest.permission.RECORD_AUDIO
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.core.graphics.createBitmap

internal class PermissionHandlingWebChromeClient(
    private val permissionDelegate: ActivityPermissionDelegate,
    private val onFileChooser: (ValueCallback<Array<Uri>>, Array<String>) -> Unit
) : WebChromeClient() {
    override fun getDefaultVideoPoster(): Bitmap = createBitmap(10, 10)

    override fun onPermissionRequest(request: PermissionRequest) {

        if (request.origin.toString() != "https://sdk.onfido.com/") {
            request.deny()
            return
        }

        val grants = mutableListOf<String>()
        val requiredPermissions = mutableListOf<String>()
        request.resources?.forEach { resource ->
            when (resource) {
                PermissionRequest.RESOURCE_VIDEO_CAPTURE -> {
                    requiredPermissions.add(CAMERA)
                    grants.add(PermissionRequest.RESOURCE_VIDEO_CAPTURE)
                }

                PermissionRequest.RESOURCE_AUDIO_CAPTURE -> {
                    requiredPermissions.add(RECORD_AUDIO)
                    grants.add(PermissionRequest.RESOURCE_AUDIO_CAPTURE)
                }
            }
        }

        val notGrantedPermissions = requiredPermissions.filter {
            !permissionDelegate.hasPermission(it)
        }

        if (notGrantedPermissions.isNotEmpty()) {
            // Filter permissions that are not permanently denied
            val permissionsToRequest = notGrantedPermissions.filter {
                !permissionDelegate.shouldShowRationale(it)
            }
            if (permissionsToRequest.isEmpty()) {
                // This means all requested permissions are denied
                request.deny()
            } else {
                permissionDelegate.registerForPermissions(
                    permissionsToRequest.toTypedArray()
                ) { result ->
                    if (result.values.any { !it }) {
                        request.deny()
                    } else {
                        request.grant(grants.toTypedArray())
                    }
                }
            }
        } else {
            request.grant(grants.toTypedArray())
        }
    }

    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>,
        fileChooserParams: FileChooserParams
    ): Boolean {
        onFileChooser(filePathCallback, fileChooserParams.acceptTypes)
        return true
    }
}
