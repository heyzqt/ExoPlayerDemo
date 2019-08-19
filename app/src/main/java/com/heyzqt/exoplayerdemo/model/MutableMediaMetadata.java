package com.heyzqt.exoplayerdemo.model;

import android.support.v4.media.MediaMetadataCompat;
import android.text.TextUtils;

/**
 * Created by heyzqt on 2019-08-18.
 */
public class MutableMediaMetadata {

    public MediaMetadataCompat metadata;
    public final String trackId;

    public MutableMediaMetadata(String trackId, MediaMetadataCompat metadata) {
        this.metadata = metadata;
        this.trackId = trackId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || obj.getClass() != MutableMediaMetadata.class) {
            return false;
        }

        MutableMediaMetadata that = (MutableMediaMetadata) obj;

        return TextUtils.equals(trackId, that.trackId);
    }
}
