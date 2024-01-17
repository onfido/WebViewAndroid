package com.onfido.evergreen

import android.Manifest.permission.CAMERA
import android.Manifest.permission.RECORD_AUDIO
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@LargeTest
class WebViewIntegrationTest {
    @get:Rule
    var permissionCamera: GrantPermissionRule = GrantPermissionRule.grant(CAMERA)

    @get:Rule
    var permissionAudio: GrantPermissionRule = GrantPermissionRule.grant(RECORD_AUDIO)

    @get:Rule
    var activityScenarioRule = activityScenarioRule<MainActivity>()

    @Test
    fun openApp() {
        onView(withId(R.id.webview)).check(matches(isDisplayed()))
    }
}
