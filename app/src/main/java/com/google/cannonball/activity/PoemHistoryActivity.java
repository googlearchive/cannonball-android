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
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.answers.ShareEvent;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.mopub.nativeads.MoPubAdAdapter;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.google.cannonball.App;
import com.google.cannonball.BuildConfig;
import com.google.cannonball.FirebaseHelpers;
import com.google.cannonball.R;
import com.google.cannonball.model.Poem;
import com.google.cannonball.model.Theme;
import com.google.cannonball.view.AvenirTextView;
import com.google.cannonball.view.ImageLoader;
import io.fabric.sdk.android.services.concurrency.AsyncTask;

public class PoemHistoryActivity extends Activity {
    private static final String TAG = "PoemHistory";
    private static final String MY_AD_UNIT_ID = BuildConfig.MOPUB_AD_UNIT_ID;
    private PoemListAdapter adapter;
    private OnShareClickListener shareListener;
    private OnDeleteClickListener deleteListener;
    private MoPubAdAdapter moPubAdAdapter;

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

        setUpViews();
    }

    private void setUpViews() {
        setUpBack();
        setUpPoemList();
    }

    private void setUpPoemList() {
        shareListener = new OnShareClickListener();
        deleteListener = new OnDeleteClickListener();

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
            shareImageView.setOnClickListener(shareListener);

            final ImageView deleteImageView = (ImageView) v.findViewById(R.id.delete);
            deleteImageView.setTag(poemId);
            deleteImageView.setOnClickListener(deleteListener);

            AvenirTextView text = (AvenirTextView) v.findViewById(R.id.poem_text);
            text.setText(poem.getText());

            text = (AvenirTextView) v.findViewById(R.id.poem_theme);
            text.setText("#" + poem.getTheme());
        }
    }

    class OnShareClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Crashlytics.log("PoemHistory: clicked to share poem with id: " + v.getTag());


            final RelativeLayout originalPoem = (RelativeLayout) v.getParent();

            final LinearLayout shareContainer = (LinearLayout) findViewById(R.id.share_container);
            if (shareContainer.getChildCount() > 0) {
                shareContainer.removeAllViews();
            }
            final RelativeLayout poem
                    = (RelativeLayout) getLayoutInflater().inflate(R.layout.listview_poem, null);

            final ImageView share = (ImageView) poem.findViewById(R.id.share);
            share.setVisibility(View.GONE);
            final ImageView delete = (ImageView) poem.findViewById(R.id.delete);
            delete.setVisibility(View.GONE);

            TextView text = (TextView) poem.findViewById(R.id.poem_text);
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

            text = (TextView) poem.findViewById(R.id.poem_theme);
            originalText = (TextView) originalPoem.findViewById(R.id.poem_theme);
            text.setTextSize(getResources().getDimensionPixelSize(R.dimen.share_text_size));
            text.setText(originalText.getText());

            final ImageView poemImage = (ImageView) poem.findViewById(R.id.poem_image);
            final ImageView originalPoemImage
                    = (ImageView) originalPoem.findViewById(R.id.poem_image);
            poemImage.setImageDrawable(originalPoemImage.getDrawable());
            poem.setTag(v.getTag());
            shareContainer.addView(poem);

            // TODO: Convert to Google Analytics for Firebase
            Answers.getInstance().logShare(new ShareEvent()
                    .putMethod("Twitter").putContentName("Poem").putContentType("tweet with image"));

            new SharePoemTask().execute(poem);
        }
    }

    class OnDeleteClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Crashlytics.log("PoemHistory: clicked to delete poem with id: " + v.getTag());
            // TODO: Convert to Google Analytics for Firebase
            Answers.getInstance().logCustom(new CustomEvent("removed poem"));
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

    class SharePoemTask extends AsyncTask<View, Void, Boolean> {
        @Override
        protected Boolean doInBackground(View... views) {
            final View poem = views[0];
            boolean result = false;

            if (App.isExternalStorageWritable()) {
                // generating image
                final Bitmap bitmap = Bitmap.createBitmap(
                        getResources().getDimensionPixelSize(R.dimen.share_width_px),
                        getResources().getDimensionPixelSize(R.dimen.share_height_px),
                        Bitmap.Config.ARGB_8888);
                final Canvas canvas = new Canvas(bitmap);
                poem.draw(canvas);

                final File picFile = App.getPoemFile("poem_" + poem.getTag() + ".jpg");

                try {
                    // TODO: this breaks because of new Android permissions model.
                    // If we want to write to external storage, we need to check and ask.
                    // Looks like there is a way to do this without external storage, though
                    // https://stackoverflow.com/questions/9049143/android-share-intent-for-a-bitmap-is-it-possible-not-to-save-it-prior-sharing
                    picFile.createNewFile();

                    final FileOutputStream picOut = new FileOutputStream(picFile);
                    final boolean saved = bitmap.compress(Bitmap.CompressFormat.JPEG, 90, picOut);
                    if (saved) {
                        final CharSequence hashtag
                                = ((TextView) poem.findViewById(R.id.poem_theme)).getText();

                        // create Uri from local image file://<absolute path>
                        final Uri imageUri = Uri.fromFile(picFile);
                        final TweetComposer.Builder builder
                                = new TweetComposer.Builder(PoemHistoryActivity.this)
                                .text(getApplicationContext().getResources()
                                        .getString(R.string.share_poem_tweet_text) + " " + hashtag)
                                .image(imageUri);
                        builder.show();

                        result = true;
                    } else {
                        Crashlytics.log(Log.ERROR, TAG, "Error when trying to save Bitmap of poem");
                        Toast.makeText(getApplicationContext(),
                                getResources().getString(R.string.toast_share_error),
                                Toast.LENGTH_SHORT).show();
                    }
                    picOut.close();
                } catch (IOException e) {
                    Crashlytics.logException(e);
                    e.printStackTrace();
                }

                poem.destroyDrawingCache();
            } else {
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.toast_share_error),
                        Toast.LENGTH_SHORT).show();
                Crashlytics.log(Log.ERROR, TAG, "External Storage not writable");
            }

            return result;
        }

    }
}
