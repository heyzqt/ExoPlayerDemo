package com.heyzqt.exoplayerdemo.model;

import android.support.v4.media.MediaMetadataCompat;

import java.util.Iterator;

/**
 * Created by heyzqt on 2019-08-18.
 */
public interface MusicProviderSource {
    String CUSTOM_METADATA_TRACK_SOURCE = "__SOURCE__";

    Iterator<MediaMetadataCompat> iterator();
}
