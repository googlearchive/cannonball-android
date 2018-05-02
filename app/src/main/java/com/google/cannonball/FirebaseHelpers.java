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
