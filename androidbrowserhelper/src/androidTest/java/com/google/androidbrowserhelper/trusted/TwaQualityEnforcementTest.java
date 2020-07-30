// Copyright 2020 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.androidbrowserhelper.trusted;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.browser.customtabs.CustomTabsCallback;
import androidx.browser.customtabs.CustomTabsSessionToken;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;
import androidx.test.rule.ActivityTestRule;

import com.google.androidbrowserhelper.trusted.testcomponents.TestActivity;
import com.google.androidbrowserhelper.trusted.testcomponents.TestBrowser;
import com.google.androidbrowserhelper.trusted.testcomponents.TestCustomTabsService;
import com.google.androidbrowserhelper.trusted.testcomponents.TestCustomTabsServiceSupportsTwas;
import com.google.androidbrowserhelper.trusted.testutils.EnableComponentsTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import static com.google.androidbrowserhelper.trusted.testutils.TestUtil.getBrowserActivityWhenLaunched;
import static org.junit.Assert.assertTrue;

/**
 * Instrumentation tests for {@link QualityEnforcementCallbackHandler}
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class TwaQualityEnforcementTest {
    private static final Uri URL = Uri.parse("https://www.test.com/default_url/");

    private Context mContext = ApplicationProvider.getApplicationContext();

    @Rule
    public final EnableComponentsTestRule mEnableComponents = new EnableComponentsTestRule(
            TestActivity.class,
            TestBrowser.class,
            TestCustomTabsServiceSupportsTwas.class,
            TestCustomTabsService.class
    );
    @Rule
    public final ActivityTestRule<TestActivity> mActivityTestRule =
            new ActivityTestRule<>(TestActivity.class, false, true);

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private TestActivity mActivity;

    private TwaLauncher mTwaLauncher;

    @Before
    public void setUp() {
        TwaProviderPicker.restrictToPackageForTesting(mContext.getPackageName());

        TestCustomTabsService.setCanCreateSessions(true);
        mActivity = mActivityTestRule.getActivity();
        mTwaLauncher = new TwaLauncher(mActivity);
    }

    @Test
    public void triggerQualityEnforcement_Notify() {
        Runnable launchRunnable = () -> mTwaLauncher.launch(URL);
        CustomTabsSessionToken token =
                CustomTabsSessionToken.getSessionTokenFromIntent(
                        getBrowserActivityWhenLaunched(launchRunnable).getIntent());
        CustomTabsCallback callback = token.getCallback();

        Bundle args = new Bundle();
        String message = "TestMessage";
        args.putString(QualityEnforcementCallbackHandler.KEY_CRASH_REASON, message);
        Bundle result =
                callback.extraCallbackWithResult(QualityEnforcementCallbackHandler.NOTIFY, args);
        assertTrue(result.getBoolean(QualityEnforcementCallbackHandler.KEY_SUCCESS));
    }

    @Test(expected = RuntimeException.class)
    public void triggerQualityEnforcement_Crash() {
        Runnable launchRunnable = () -> mTwaLauncher.launch(URL);
        CustomTabsSessionToken token =
                CustomTabsSessionToken.getSessionTokenFromIntent(
                        getBrowserActivityWhenLaunched(launchRunnable).getIntent());
        CustomTabsCallback callback = token.getCallback();

        Bundle args = new Bundle();
        String message = "TestMessage";
        args.putString(QualityEnforcementCallbackHandler.KEY_CRASH_REASON, message);
        callback.extraCallbackWithResult(QualityEnforcementCallbackHandler.CRASH, args);
    }
}
