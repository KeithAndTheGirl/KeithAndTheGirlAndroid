package com.keithandthegirl.app.ui.gallery;

import android.os.Parcel;
import android.os.Parcelable;

/**
* Created by Jeff on 8/31/2014.
* Copyright JeffInMadison.com 2014
*/
public class ImageGalleryInfoHolder implements Parcelable {
    private String mImageUrl;
    private String mCaption;
    private boolean mIsExplicit;

    public ImageGalleryInfoHolder() {}

    public ImageGalleryInfoHolder(String imageUrl, String caption) {
        mImageUrl = imageUrl;
        mCaption = caption;
        mIsExplicit = false;
    }

    public boolean isExplicit() {
        return mIsExplicit;
    }

    public void setExplicit(boolean isExplicit) {
        mIsExplicit = isExplicit;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(final String imageUrl) {
        mImageUrl = imageUrl;
    }

    public String getCaption() {
        return mCaption;
    }

    public void setCaption(final String caption) {
        mCaption = caption;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mImageUrl);
        dest.writeString(this.mCaption);
    }

    ImageGalleryInfoHolder(Parcel in) {
        this.mImageUrl = in.readString();
        this.mCaption = in.readString();
    }

    public static final Creator<ImageGalleryInfoHolder> CREATOR = new Creator<ImageGalleryInfoHolder>() {
        public ImageGalleryInfoHolder createFromParcel(Parcel source) {
            return new ImageGalleryInfoHolder(source);
        }

        public ImageGalleryInfoHolder[] newArray(int size) {
            return new ImageGalleryInfoHolder[size];
        }
    };
}
