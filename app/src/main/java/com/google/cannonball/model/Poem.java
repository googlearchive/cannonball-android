package com.google.cannonball.model;

/**
 * Created by jhuleatt on 3/13/18.
 */

public class Poem {
    public String text;
    public int imageId;
    public String theme;
    public long creationTimeStamp;

    // for Firebase indexing
    public long inverseCreationTimeStamp;

    // Firebase needs this
    public Poem() {}

    public Poem(String text, int imageId, String theme, long creationTimeStamp) {
        this.text = text;
        this.imageId = imageId;
        this.theme = theme;
        this.creationTimeStamp = creationTimeStamp;
        this.inverseCreationTimeStamp = -1 * creationTimeStamp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public long getCreationTimeStamp() {
        return creationTimeStamp;
    }

    public void setCreationTimeStamp(long creationTimeStamp) {
        this.creationTimeStamp = creationTimeStamp;
    }

    public long getInverseCreationTimeStamp() {
        return inverseCreationTimeStamp;
    }

    public void setInverseCreationTimeStamp(long inverseCreationTimeStamp) {
        this.inverseCreationTimeStamp = inverseCreationTimeStamp;
    }
}
