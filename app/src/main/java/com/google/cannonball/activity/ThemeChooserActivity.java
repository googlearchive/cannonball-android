/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import com.google.cannonball.App;
import com.google.cannonball.R;
import com.google.cannonball.model.Theme;
import com.google.cannonball.view.ThemeAdapter;

public class ThemeChooserActivity extends Activity {
    public static final String IS_NEW_POEM = "ThemeChooser.IS_NEW_POEM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_chooser);
        setUpViews();
    }

    private void setUpViews() {
        setUpHistory();
        setUpIcon();
        setUpThemes();
    }

    private void setUpIcon() {
        final ImageView icon = (ImageView) findViewById(R.id.cannonball_logo);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crashlytics.log("ThemeChooser: clicked About button");
                Answers.getInstance().logCustom(new CustomEvent("clicked about"));
                final Intent intent = new Intent(ThemeChooserActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setUpHistory() {
        final ImageView history = (ImageView) findViewById(R.id.history);
        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crashlytics.log("ThemeChooser: clicked History button");
                Answers.getInstance().logCustom(new CustomEvent("clicked history"));
                final Intent intent = new Intent(ThemeChooserActivity.this,
                        PoemHistoryActivity.class);
                intent.putExtra(IS_NEW_POEM, false);
                startActivity(intent);
            }
        });
    }

    private void setUpThemes() {
        final ListView view = (ListView) findViewById(R.id.theme_list);
        view.setAdapter(new ThemeAdapter(this, Theme.values()));
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Theme theme = Theme.values()[position];
                Crashlytics.log("ThemeChooser: clicked on Theme: " + theme.getDisplayName());
                Crashlytics.setString(App.CRASHLYTICS_KEY_THEME, theme.getDisplayName());
                Answers.getInstance().logCustom(new CustomEvent("clicked build poem").putCustomAttribute("theme", theme.getDisplayName()));
                final Intent intent = new Intent(getBaseContext(), PoemBuilderActivity.class);
                intent.putExtra(PoemBuilderActivity.KEY_THEME, theme);
                startActivity(intent);
            }
        });
    }
}
