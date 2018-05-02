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
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.cannonball.App;
import com.google.cannonball.FirebaseHelpers;
import com.google.cannonball.R;
import com.google.cannonball.model.Poem;
import com.google.cannonball.model.Theme;
import com.google.cannonball.view.AvenirTextView;
import com.google.cannonball.view.ImageLoader;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PoemHistoryActivity extends Activity {
    private static final String TAG = "PoemHistory";
    private PoemListAdapter adapter;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Crashlytics.log("PoemHistory: getting back to ThemeChooser");
        if (getIntent().getBooleanExtra(ThemeChooserActivity.IS_NEW_POEM, false)) {
            final Intent intent = new Intent(getApplicationContext(), ThemeChooserActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poem_history);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        setUpViews();
    }

    private void setUpViews() {
        setUpBack();
        setUpPoemList();
    }

    private void setUpPoemList() {
        final ListView poemsList = (ListView) findViewById(R.id.poem_history_list);

        adapter = new PoemListAdapter(FirebaseHelpers.getUserListOptions(R.layout.listview_poem));

        poemsList.setAdapter(adapter);
    }

    private void setUpBack() {
        final ImageView back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        adapter.startListening();
        final IntentFilter intentFilter = new IntentFilter(App.BROADCAST_POEM_DELETION);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        adapter.stopListening();
        super.onDestroy();
    }

    class PoemListAdapter extends FirebaseListAdapter<Poem> {
        public PoemListAdapter(@NonNull FirebaseListOptions<Poem> options) {
            super(options);
        }

        @Override
        protected void populateView(View v, Poem poem, int position) {
            final ImageView image = (ImageView) v.findViewById(R.id.poem_image);
            // TODO optimize that to avoid getIdentifier call
            try {
                final Theme t = Theme.valueOf(poem.getTheme().toUpperCase());
                final int poemImage = t.getImageList().get(poem.getImageId());
                image.post(new Runnable() {
                    @Override
                    public void run() {
                        ImageLoader.getImageLoader().load(poemImage, image);
                    }
                });
            } catch (Resources.NotFoundException ex) {
                //In case an identifier is removed from the list
            }

            String poemId = this.getRef(position).getKey();

            final ImageView shareImageView = (ImageView) v.findViewById(R.id.share);
            shareImageView.setTag(poemId);
            shareImageView.setOnClickListener(new OnShareClickListener(poem));

            final ImageView deleteImageView = (ImageView) v.findViewById(R.id.delete);
            deleteImageView.setTag(poemId);
            deleteImageView.setOnClickListener(new OnDeleteClickListener(poem));

            AvenirTextView text = (AvenirTextView) v.findViewById(R.id.poem_text);
            text.setText(poem.getText());

            text = (AvenirTextView) v.findViewById(R.id.poem_theme);
            text.setText("#" + poem.getTheme());
        }
    }

    class OnShareClickListener implements View.OnClickListener {
        Poem poem;

        public OnShareClickListener (Poem poem) {
            this.poem = poem;
        }
        @Override
        public void onClick(View v) {
            Crashlytics.log("PoemHistory: clicked to share poem with id: " + v.getTag());

            final RelativeLayout originalPoem = (RelativeLayout) v.getParent();

            final LinearLayout shareContainer = (LinearLayout) findViewById(R.id.share_container);
            if (shareContainer.getChildCount() > 0) {
                shareContainer.removeAllViews();
            }
            final RelativeLayout poemLayout
                    = (RelativeLayout) getLayoutInflater().inflate(R.layout.listview_poem, null);

            final ImageView share = (ImageView) poemLayout.findViewById(R.id.share);
            share.setVisibility(View.GONE);
            final ImageView delete = (ImageView) poemLayout.findViewById(R.id.delete);
            delete.setVisibility(View.GONE);

            TextView text = (TextView) poemLayout.findViewById(R.id.poem_text);
            TextView originalText = (TextView) originalPoem.findViewById(R.id.poem_text);
            text.setTextSize(getResources().getDimensionPixelSize(R.dimen.share_text_size));
            final int padding = getResources().getDimensionPixelSize(R.dimen.share_text_padding);
            text.setPadding(padding, padding, padding,
                    getResources().getDimensionPixelSize(R.dimen.share_text_margin_bottom));
            final RelativeLayout.LayoutParams params
                    = (RelativeLayout.LayoutParams) text.getLayoutParams();
            params.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params.removeRule(RelativeLayout.ALIGN_PARENT_START);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            text.setLayoutParams(params);
            text.setText(originalText.getText());

            text = (TextView) poemLayout.findViewById(R.id.poem_theme);
            originalText = (TextView) originalPoem.findViewById(R.id.poem_theme);
            text.setTextSize(getResources().getDimensionPixelSize(R.dimen.share_text_size));
            text.setText(originalText.getText());

            final ImageView poemImage = (ImageView) poemLayout.findViewById(R.id.poem_image);
            final ImageView originalPoemImage
                    = (ImageView) originalPoem.findViewById(R.id.poem_image);
            poemImage.setImageDrawable(originalPoemImage.getDrawable());

            // render poemlayout to bitmap
            poemLayout.measure(getResources().getDimensionPixelSize(R.dimen.share_width_px), getResources().getDimensionPixelSize(R.dimen.share_height_px));
            final Bitmap bitmap = Bitmap.createBitmap(
                    poemLayout.getMeasuredWidth(),
                    poemLayout.getMeasuredHeight(),
                    Bitmap.Config.ARGB_8888);
            final Canvas canvas = new Canvas(bitmap);
            poemLayout.layout(0,0,poemLayout.getMeasuredWidth(), poemLayout.getMeasuredHeight());
            poemLayout.draw(canvas);

            // save rendered poem to file
            try {
                File cachePath = new File(PoemHistoryActivity.this.getCacheDir(), "rendered_poems");
                cachePath.mkdirs(); // don't forget to make the directory
                FileOutputStream stream = new FileOutputStream(cachePath + "/poem_img.png"); // overwrites this image every time
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // get URI for newly rendered image
            File imagePath = new File(PoemHistoryActivity.this.getCacheDir(), "rendered_poems");
            File newFile = new File(imagePath, "/poem_img.png");
            Uri contentUri = FileProvider.getUriForFile(PoemHistoryActivity.this, "com.google.cannonball.fileprovider", newFile);

            // use native OS share
            if (contentUri != null) {
                Log.d(TAG, contentUri.toString());
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
                shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                startActivity(Intent.createChooser(shareIntent, "Choose an app"));
            }

            // log to Analytics
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "poem_image");
            bundle.putString(FirebaseAnalytics.Param.METHOD, "native_share");
            bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, this.poem.getTheme());
            bundle.putInt("length", this.poem.getText().split("\\s+").length);

            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle);
        }
    }

    class OnDeleteClickListener implements View.OnClickListener {
        Poem poem;

        public OnDeleteClickListener (Poem poem) {
            this.poem = poem;
        }

        @Override
        public void onClick(View v) {
            Crashlytics.log("PoemHistory: clicked to delete poem with id: " + v.getTag());

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, poem.getTheme());

            FirebaseHelpers.deletePoem((String) v.getTag()).addOnCompleteListener(new OnCompleteListener<Void>() {
                  @Override
                  public void onComplete(@NonNull Task<Void> task) {
                      if (task.isSuccessful()) {
                          Toast.makeText(getApplicationContext(),
                                  "Poem deleted!", Toast.LENGTH_SHORT)
                                  .show();
                          final Intent i = new Intent(getApplicationContext(), PoemHistoryActivity.class);
                          i.putExtra(ThemeChooserActivity.IS_NEW_POEM, true);
                          startActivity(i);
                      } else {
                          Toast.makeText(getApplicationContext(),
                                  "Problem deleting poem", Toast.LENGTH_SHORT)
                                  .show();
                      }
                  }
              }
            );
        }
    }
}
