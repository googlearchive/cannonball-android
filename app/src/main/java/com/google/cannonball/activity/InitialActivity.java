/**
 * Copyright (C) 2014 Twitter Inc and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cannonball.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;
import java.util.List;


public class InitialActivity extends Activity {
    private static final int RC_SIGN_IN = 3294845;
    private static final String TAG = "InitialActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init Firebase
        // FirebaseApp.initializeApp(this)

        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            startThemeActivity();
        } else {
            startLoginActivity();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "GOT AN ACTIVITY RESULT");
        if (requestCode == RC_SIGN_IN) {
            Log.d(TAG, "IT WAS A SIGN IN RESULT");
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK) {
                startThemeActivity();
                finish();
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    showError("Sign in cancelled");
                    return;
                }

                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showError("Sign in cancelled");
                    return;
                }

                showError("Unknown Error");
                Log.e(TAG, "Sign-in error: ", response.getError());
            }
        }
    }

    private void showError(String message) {
        Toast.makeText(getApplicationContext(), "Sign in cancelled",
                Toast.LENGTH_SHORT).show();
    }

    private void startThemeActivity() {
        startActivity(new Intent(this, ThemeChooserActivity.class));
    }

    private void startLoginActivity() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build()
        );

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN
        );
    }
}
