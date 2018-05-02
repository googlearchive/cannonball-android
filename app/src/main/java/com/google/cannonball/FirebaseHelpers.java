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

package com.google.cannonball;

import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.gms.tasks.Task;
import com.google.cannonball.model.Poem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

/**
 * Created by jhuleatt on 3/13/18.
 */

public class FirebaseHelpers {
    public static String getUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            return null;
        } else {
            return user.getUid();
        }
    }

    public static Task<Void> savePoem(Poem newPoem) {
        String userId = getUserId();
        DatabaseReference newPoemRef = FirebaseDatabase.getInstance().getReference(userId).push();
        return newPoemRef.setValue(newPoem);
    }

    public static Task<Void> deletePoem(String key) {
        String userId = getUserId();
        DatabaseReference newPoemRef = FirebaseDatabase.getInstance().getReference(userId).child(key);

        return newPoemRef.removeValue();
    }

    public static FirebaseListOptions<Poem> getUserListOptions(int layout) {
        String userId = getUserId();
        DatabaseReference poemsRef = FirebaseDatabase.getInstance().getReference(userId);
        Query poemsQuery = poemsRef.orderByChild("inverseCreationTimeStamp");

        return new FirebaseListOptions.Builder<Poem>()
                .setQuery(poemsQuery, Poem.class)
                .setLayout(layout)
                .build();
    }
}
