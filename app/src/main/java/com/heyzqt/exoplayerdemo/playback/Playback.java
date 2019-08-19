package com.heyzqt.exoplayerdemo.playback;

/**
 * 通知音频播放
 * Created by heyzqt on 2019-08-19.
 */
public interface Playback {

    void start();

    void stop();

    boolean isPlaying();

    /**
     * 获取当前的播放进度
     *
     * @return
     */
    long getCurrentStreamPosition();

    void play();

    void pause();

    void setState(int state);

    int getState();

    void seekTo(long position);

    void setCurrentMediaId(String mediaId);

    String getCurrentMediaId();

    interface Callback {

        void onCompletion();

        void onPlaybackStatusChanged(int state);

        void onError();

        void setCurrentMediaId(String mediaId);
    }

    void setCallback(Callback callback);
}
