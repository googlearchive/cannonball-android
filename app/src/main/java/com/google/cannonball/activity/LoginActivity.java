/**
 * Copyright (C) 2017 Google Inc and other contributors.
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
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.cannonball.R;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends Activity {
    private static final int RC_SIGN_IN = 1337;
    private Button phoneButton;
    private static final String TAG = "LoginActivity";
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setUpViews();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "GOT AN ACTIVITY RESULT");
        if (requestCode == RC_SIGN_IN) {
            Log.d(TAG, "IT WAS A SIGN IN RESULT");
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK) {
                logAuthEvent("phone");
                startThemeChooser();
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

    private void setUpViews() {
        setUpAnonymousButton();
        setUpPhoneAuthButton();
    }

    private void setUpPhoneAuthButton() {
        phoneButton = (Button) findViewById(R.id.phone_button);

        phoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });
    }

    private void setUpAnonymousButton() {
        TextView skipButton;
        skipButton = (TextView) findViewById(R.id.anonymous);
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crashlytics.log("Login: skipped login");
                FirebaseAuth.getInstance().signInAnonymously()
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInAnonymously:success");
                                    logAuthEvent("anonymous");
                                    startThemeChooser();
                                    overridePendingTransition(R.anim.slide_down, R.anim.slide_up);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInAnonymously:failure", task.getException());
                                    Toast.makeText(LoginActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    private void logAuthEvent(String AuthMethod) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.METHOD, AuthMethod);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
    }

    private void startThemeChooser() {
        final Intent themeChooserIntent = new Intent(LoginActivity.this,
                ThemeChooserActivity.class);
        startActivity(themeChooserIntent);
    }
}
