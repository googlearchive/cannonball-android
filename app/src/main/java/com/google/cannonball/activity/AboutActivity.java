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
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.cannonball.App;
import com.google.cannonball.R;
import com.google.firebase.analytics.FirebaseAnalytics;

public class AboutActivity extends Activity {
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setUpViews();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    private void setUpViews() {
        setUpCrashSwitcher();
        setUpSignOut();
    }

    private void setUpSignOut() {
        final TextView bt = (TextView) findViewById(R.id.deactivate_accounts);
        final Context ctx = this;

        mFirebaseAnalytics.logEvent("logout", null);

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthUI.getInstance()
                        .signOut(ctx)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(Task<Void> Task) {
                                Toast.makeText(getApplicationContext(), "Signed out",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void setUpCrashSwitcher() {
        final CheckBox cb = (CheckBox) findViewById(R.id.activate_crashes);
        cb.setChecked(App.getInstance().areCrashesEnabled());
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                App.getInstance().setCrashesStatus(isChecked);
                Toast.makeText(getApplicationContext(), "Crashes are " +
                        (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
                Crashlytics.setBool(App.CRASHLYTICS_KEY_CRASHES, isChecked);
            }
        });
    }

}
