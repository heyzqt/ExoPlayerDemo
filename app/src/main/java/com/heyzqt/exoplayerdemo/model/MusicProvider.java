package com.heyzqt.exoplayerdemo.model;

import android.os.AsyncTask;
import android.support.v4.media.MediaMetadataCompat;

import com.heyzqt.exoplayerdemo.utils.LogHelper;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by heyzqt on 2019-08-18.
 * 进一步封装MediaMetadataCompat，通过id查询MediaMetadataCompat
 */
public class MusicProvider {
    private static final String TAG = LogHelper.makeLogTag(MusicProvider.class);

    private MusicProviderSource mSource;

    private ConcurrentHashMap<String, MutableMediaMetadata> mMusicListById;

    enum State {
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }

    private volatile State mCurrentState = State.NON_INITIALIZED;

    public interface Callback {
        void onMusicCatalogReady(boolean success);
    }

    public MusicProvider() {
        this(new RemoteJSONSource());//获取服务器音乐数据
    }

    public MusicProvider(MusicProviderSource source) {
        this.mSource = source;
        mMusicListById = new ConcurrentHashMap<>();
    }

    public MediaMetadataCompat getMusic(String musicId) {
        return mMusicListById.containsKey(musicId) ? mMusicListById.get(musicId).metadata : null;
    }

    //异步加载音乐数据
    public void retrieveMediaAsync(final Callback callback) {
        if (mCurrentState == State.INITIALIZED) {
            if (callback != null) {
                callback.onMusicCatalogReady(true);
            }
            return;
        }

        //开启异步线程加载音乐数据
        new AsyncTask<Void, Void, State>() {

            @Override
            protected State doInBackground(Void... voids) {
                retrieveMedia();
                return mCurrentState;
            }

            @Override
            protected void onPostExecute(State state) {
                super.onPostExecute(state);
                if (callback != null) {
                    callback.onMusicCatalogReady(state == State.INITIALIZED);
                }
            }
        }.execute();
    }

    private synchronized void retrieveMedia() {
        try {
            if (mCurrentState == State.NON_INITIALIZED) {
                mCurrentState = State.INITIALIZING;
            }

            Iterator<MediaMetadataCompat> tracks = mSource.iterator();
            while (tracks.hasNext()) {
                MediaMetadataCompat item = tracks.next();
                String musicId = item.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
                mMusicListById.put(musicId, new MutableMediaMetadata(musicId, item));
            }
            mCurrentState = State.INITIALIZED;
        } finally {
            if (mCurrentState != State.INITIALIZED) {
                //发生某些意外情况，我们需要把状态重置为NON_INITIALIZED
                mCurrentState = State.NON_INITIALIZED;
            }
        }
    }
}
