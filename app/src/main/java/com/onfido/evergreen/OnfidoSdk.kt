package com.onfido.evergreen

import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.onfido.Onfido
import com.onfido.models.Applicant
import com.onfido.models.SdkToken
import com.onfido.models.WorkflowRun
import io.github.cdimascio.dotenv.dotenv

class OnfidoSdk(private var activity: MainActivity, private var webView: WebView) {
    private var sdkLoadedCallback: (() -> Unit)? = null
    private var initialized: Boolean = false
    private var sdkLoaded: Boolean = false
    private var eventHandler: EventHandler? = null

    private val dotenv = dotenv { directory = "./assets"; filename = "env" }
    private val onfidoApi = Onfido.builder()
        .apiToken(dotenv["API_KEY"])
        .regionEU()
        .build()

    fun getApplicantId(): String {
        return onfidoApi
            .applicant
            .create(
                Applicant.request()
                    .firstName("Capture")
                    .lastName("Testing")
            ).id
    }

    fun getSdkToken(applicantId: String): String {
        return onfidoApi
            .sdkToken
            .generate(
                SdkToken.request()
                    .applicantId(applicantId)
            )
    }

    fun getWorkflowRunId(applicantId: String): String {
        return onfidoApi
            .workflowRun
            .create(
                WorkflowRun.request()
                    .applicantId(applicantId)
                    .workflowId(dotenv["WORKFLOW_ID"])
            ).id
    }

    fun initSdk(parameter: String, handler: EventHandler) {
        if (initialized) {
            error("Sdk cannot be initialized multiple times")
        }

        this.eventHandler = handler
        initialized = true

        if (sdkLoaded) {
            this.bootstrapSdk(parameter)
        } else {
            this.sdkLoadedCallback = {
                this.bootstrapSdk(parameter)
            }
        }
    }

    private fun bootstrapSdk(parameter: String) {
        val js = """
        try {
            Onfido.init({
                ...$parameter,
                onComplete: (data) => sdk.onComplete(JSON.stringify(data)),
                onError: (e) => sdk.onError(JSON.stringify(e))
            })
        } catch (e) {
            console.error(e)
            sdk.onError(JSON.stringify({message: e.message}))
        }
        """.trimIndent()

        webView.evaluateJavascript(js, null)
    }

    private fun runOnUiThread(action: () -> Unit) {
        activity.runOnUiThread(action)
    }

    fun getSdkCallbackJsInterface(): Any {
        return object {
            @JavascriptInterface
            fun loadComplete() {
                sdkLoaded = true
                runOnUiThread {
                    sdkLoadedCallback?.invoke()
                }
            }

            @JavascriptInterface
            fun loadError(error: String?) {
                // FIXME: error handling
                if (error != null) {
                    Log.e(OnfidoSdk::class.java.simpleName, error)
                }
            }

            @JavascriptInterface
            fun onComplete(data: String?) {
                eventHandler?.onComplete?.let {
                    runOnUiThread { it(data) }
                }
            }

            @JavascriptInterface
            fun onError(e: String?) {
                eventHandler?.onError?.let {
                    runOnUiThread { it(e) }
                }
            }

            @JavascriptInterface
            fun getUserMedia() {
                runOnUiThread {
//                     TODO request permissions that might be needed
//                    requestPermissions()
                }
            }
        }
    }
}