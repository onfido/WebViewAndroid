package com.onfido.evergreen

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.webkit.WebView
import android.webkit.ValueCallback
import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.PermissionRequest
import android.content.Intent
import android.provider.MediaStore
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.FileProvider
import android.webkit.WebViewClient
import android.app.ProgressDialog
import android.graphics.Bitmap
import kotlin.Throws
import android.app.Activity
import android.net.Uri
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.core.app.ActivityCompat
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private var mFilePathCallback: ValueCallback<Array<Uri>>? = null
    private var mCameraPhotoPath: Uri? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)


        with(webView) {
            settings.javaScriptEnabled = true
            settings.loadWithOverviewMode = true
            settings.allowFileAccess = true
            settings.mediaPlaybackRequiresUserGesture = false
            webViewClient = Client()
            webChromeClient = ChromeClient()

            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            loadUrl("https://crowd-testing.eu.onfido.app/f/da5ed749-9d93-4026-bf43-4e96c02e5f15")
        }

        requestPermissions(this)
    }

    inner class ChromeClient : WebChromeClient() {
        override fun onPermissionRequest(request: PermissionRequest) {
            val permissions = arrayOf(
                PermissionRequest.RESOURCE_AUDIO_CAPTURE,
                PermissionRequest.RESOURCE_VIDEO_CAPTURE
            )
            request.grant(permissions)
            requestPermissions(this@MainActivity)
        }

        //For Android 5.0+
        @SuppressLint("QueryPermissionsNeeded")
        override fun onShowFileChooser(
            webView: WebView, filePathCallback: ValueCallback<Array<Uri>>,
            fileChooserParams: FileChooserParams
        ): Boolean {
            if (mFilePathCallback != null) {
                mFilePathCallback!!.onReceiveValue(null)
            }
            mFilePathCallback = filePathCallback
            var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent!!.resolveActivity(this@MainActivity.packageManager) != null) {
                var photoFile: File? = null
                try {
                    photoFile = createImageFile()
                    val resInfoList = packageManager.queryIntentActivities(
                        takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY
                    )
                    for (resolveInfo in resInfoList) {
                        val packageName = resolveInfo.activityInfo.packageName
                        grantUriPermission(
                            packageName,
                            Uri.fromFile(photoFile),
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    }
                    takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath)
                } catch (ex: IOException) {
                    Log.e(TAG, "Failed create image", ex)
                }
                if (photoFile != null) {
                    mCameraPhotoPath =
                        if (applicationInfo.targetSdkVersion > Build.VERSION_CODES.M) {
                            val authority = "com.onfido.evergreen"
                            FileProvider.getUriForFile(this@MainActivity, authority, photoFile)
                        } else {
                            Uri.fromFile(photoFile)
                        }
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraPhotoPath)
                } else {
                    takePictureIntent = null
                }
            }
            val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
            contentSelectionIntent.type = "*/*"
            val intentArray = takePictureIntent?.let { arrayOf(it) } ?: emptyArray()
            val chooserIntent = Intent(Intent.ACTION_CHOOSER)
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Select an action")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
            startActivityForResult(chooserIntent, FILE_CHOOSER_RESULT_CODE)
            return true
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        // Check if the key event was the Back button and if there's history
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event)
    }

    inner class Client : WebViewClient() {
        private var progressDialog: ProgressDialog? = null

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            if (progressDialog == null) {
                progressDialog = ProgressDialog(this@MainActivity)
                progressDialog!!.setMessage("Loading...")
                progressDialog!!.show()
            }
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            try {
                if (progressDialog != null && progressDialog!!.isShowing) {
                    progressDialog!!.dismiss()
                    progressDialog = null
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
    }

    // Create an image file
    @Throws(IOException::class)
    private fun createImageFile(): File {
        @SuppressLint("SimpleDateFormat") val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss").format(
                Date()
            )
        val imageFileName = "img_" + timeStamp + "_"
        val storageDir = this@MainActivity.filesDir
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        var results: Array<Uri>? = null
        //Check if response is positive
        if (resultCode == RESULT_OK) {
            if (requestCode == FILE_CHOOSER_RESULT_CODE) {
                if (null == mFilePathCallback) {
                    return
                }
                //camera selected
                //Capture Photo if no image available
                if (mCameraPhotoPath != null) {
                    results = arrayOf(mCameraPhotoPath!!)
                } else {
                    //gallery selected
                    val dataString = intent!!.dataString
                    if (dataString != null) {
                        results = arrayOf(Uri.parse(dataString))
                    }
                }
            }
        }
        mFilePathCallback!!.onReceiveValue(results)
        mFilePathCallback = null
    }

    companion object {
        private const val FILE_CHOOSER_RESULT_CODE = 1
        private val TAG = MainActivity::class.java.simpleName
        private const val REQUEST_PERMISSIONS = 1
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS
        )

        fun requestPermissions(activity: Activity?) {
            // Check if we have read or write permission
            val cameraPermission =
                ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.CAMERA)
            val audioPermission =
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
            val modifyAudioPermission = ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.MODIFY_AUDIO_SETTINGS
            )
            if (cameraPermission != PackageManager.PERMISSION_GRANTED || audioPermission != PackageManager.PERMISSION_GRANTED || modifyAudioPermission != PackageManager.PERMISSION_GRANTED) {
                // We don't have permission so prompt the user
                ActivityCompat.requestPermissions(
                    activity,
                    REQUIRED_PERMISSIONS,
                    REQUEST_PERMISSIONS
                )
            }
        }
    }
}