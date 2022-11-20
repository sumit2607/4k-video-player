package com.demo.mxplayer.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Track implements Parcelable, Serializable {
    public static final Creator<Track> CREATOR = new Creator<Track>() {
        public Track createFromParcel(Parcel in) {
            return new Track(in);
        }

        public Track[] newArray(int size) {
            return new Track[size];
        }
    };
    private boolean isVideo = true;
    private String mAlbumId = "";
    private String mChannelId;
    private String mChannelTitle;
    private String mDuration = "";
    private String mId;
    private String mPath;
    private int mPosition;
    private String mPublisgedAt;
    private String mThumbnailURL;
    private String mTitle;
    private String mVideoType;

    public int describeContents() {
        return 0;
    }
    public Track(){}
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.mId);
        parcel.writeString(this.mTitle);
        parcel.writeString(this.mChannelId);
        parcel.writeString(this.mChannelTitle);
        parcel.writeString(this.mVideoType);
        parcel.writeString(this.mDuration);
        parcel.writeString(this.mThumbnailURL);
        parcel.writeString(this.mPath);
        parcel.writeInt(this.isVideo ? 1 : 0);
        parcel.writeInt(this.mPosition);
        parcel.writeString(this.mPublisgedAt);
        parcel.writeString(this.mAlbumId + "");
    }

    public static Creator<Track> getCreator() {
        return CREATOR;
    }

    public Track(Parcel parcel) {
        boolean z = true;
        this.mId = parcel.readString();
        this.mTitle = parcel.readString();
        this.mChannelId = parcel.readString();
        this.mChannelTitle = parcel.readString();
        this.mVideoType = parcel.readString();
        this.mDuration = parcel.readString();
        this.mThumbnailURL = parcel.readString();
        this.mPath = parcel.readString();
        if (parcel.readInt() != 1) {
            z = false;
        }
        this.isVideo = z;
        this.mPosition = parcel.readInt();
        this.mPublisgedAt = parcel.readString();
        this.mAlbumId = parcel.readString();
    }

    public String getTitle() {
        return this.mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getPath() {
        return this.mPath;
    }

    public void setPath(String path) {
        this.mPath = path;
    }

    public String getThumbnailURL() {
        return this.mThumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.mThumbnailURL = thumbnailURL;
    }

    public String getId() {
        return this.mId;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public int hashCode() {
        int i = 0;
        int hashCode = ((((((this.mId == null ? 0 : this.mId.hashCode()) + 31) * 31) + (this.mPath == null ? 0 : this.mPath.hashCode())) * 31) + (this.mThumbnailURL == null ? 0 : this.mThumbnailURL.hashCode())) * 31;
        if (this.mTitle != null) {
            i = this.mTitle.hashCode();
        }
        return hashCode + i;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Track other = (Track) obj;
        if (this.mId == null) {
            if (other.mId != null) {
                return false;
            }
        } else if (!this.mId.equals(other.mId)) {
            return false;
        }
        if (this.mPath == null) {
            if (other.mPath != null) {
                return false;
            }
        } else if (!this.mPath.equals(other.mPath)) {
            return false;
        }
        if (this.mThumbnailURL == null) {
            if (other.mThumbnailURL != null) {
                return false;
            }
        } else if (!this.mThumbnailURL.equals(other.mThumbnailURL)) {
            return false;
        }
        if (this.mTitle == null) {
            if (other.mTitle != null) {
                return false;
            }
            return true;
        } else if (this.mTitle.equals(other.mTitle)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isVideo() {
        return this.isVideo;
    }

    public void setVideo(boolean b) {
        this.isVideo = b;
    }

    public void setDuration(String duration) {
        this.mDuration = duration;
    }

    public void setPublisgedAt(String duration) {
        this.mPublisgedAt = duration;
    }

    public String getPublisgedAt() {
        return this.mPublisgedAt;
    }

    public String getDuration() {
        return this.mDuration;
    }

    public String getType() {
        return this.mVideoType;
    }

    public void setType(String type) {
        this.mVideoType = type;
    }

    public String getName() {
        return this.mTitle;
    }

    public void setChannelId(String channelId) {
        this.mChannelId = channelId;
    }

    public String getChannelId() {
        return this.mChannelId;
    }

    public void setChannelTitle(String channelTitle) {
        this.mChannelTitle = channelTitle;
    }

    public String getChannelTitle() {
        return this.mChannelTitle;
    }

    public void setPosition(int position) {
        this.mPosition = position;
    }

    public int getPosition() {
        return this.mPosition;
    }

    public String getAlbumId() {
        return this.mAlbumId;
    }

    public void setAlbumId(String albumId) {
        this.mAlbumId = albumId;
    }
}
