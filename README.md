# WebView Android
A sample Android native app integration with Onfido’s [Web SDK](https://documentation.onfido.com/sdk/web/), using [Android WebView] (https://developer.android.com/reference/android/webkit/WebView) component.

## Summary

This app is a simple demonstration of the minimum configurations that are required to integrate with [onfido-sdk-ui](https://documentation.onfido.com/sdk/web/) using Android native WebView component. The example uses [Smart Capture Link](https://developers.onfido.com/guide/smart-capture-link).

You can find more detailed documentation here:
- [WebView](https://docs.usercentrics.com/cmp_in_app_sdk/latest/features/webview-continuity/)

- [onfido-sdk-ui](https://documentation.onfido.com/sdk/web/)

- [Smart Capture Link](https://developers.onfido.com/guide/smart-capture-link)



## Permissions

### Android

- You will need to provide the following permissions in your `AndroidManifest.xml` file:

```AndroidManifest.xml
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.CAMERA" />
```    

- Also you will need to implement `WebChromeClient` to provide a way to provide required permissions 
and implement how do you want to handle the file chooser and capturing image from camera as this is not implemented by default in `WebView`.
For reference check the `MainActivity.kt` code sample.