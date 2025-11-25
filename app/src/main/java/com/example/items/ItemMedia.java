package blogtalk.com.items;

import android.net.Uri;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ItemMedia {

    Uri mediaUrl;
    int mediaType;
    long mediaDuration, mediaSize;

    public ItemMedia(Uri mediaUrl, int mediaType, long mediaDuration, long mediaSize) {
        this.mediaUrl = mediaUrl;
        this.mediaType = mediaType;
        this.mediaDuration = mediaDuration;
        this.mediaSize = mediaSize;
    }

    public Uri getMediaUrl() {
        return mediaUrl;
    }

    public int getMediaType() {
        return mediaType;
    }

    public long getMediaDuration() {
        return mediaDuration;
    }

    public long getMediaSize() {
        return mediaSize;
    }
}
