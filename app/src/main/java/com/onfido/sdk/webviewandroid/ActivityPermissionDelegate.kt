package com.onfido.sdk.webviewandroid

import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

private typealias PermissionCallback = (result: Map<String, Boolean>) -> Unit

internal class ActivityPermissionDelegate(
    private val activity: ComponentActivity,
) {
    private var permissionCallback: PermissionCallback? = null

    private val permissionResult =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { result: Map<String, Boolean> ->
            permissionCallback?.invoke(result)
        }

    private fun requestPermissions(permissions: Array<String>) {
        permissionResult.launch(permissions)
    }

    private fun registerForResult(callback: PermissionCallback) {
        permissionCallback = callback
    }

    fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(
            activity,
            permission,
        ) == PackageManager.PERMISSION_GRANTED

    fun shouldShowRationale(permission: String): Boolean =
        ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)

    fun registerForPermissions(
        permissions: Array<String>,
        callback: (result: Map<String, Boolean>) -> Unit,
    ) {
        registerForResult(callback)
        requestPermissions(permissions)
    }
}
