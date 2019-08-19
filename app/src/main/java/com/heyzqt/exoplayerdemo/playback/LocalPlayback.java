package com.heyzqt.exoplayerdemo.playback;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.support.v4.media.session.PlaybackStateCompat;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.heyzqt.exoplayerdemo.model.MusicProvider;
import com.heyzqt.exoplayerdemo.utils.LogHelper;

/**
 * Created by heyzqt on 2019-08-19.
 */
public class LocalPlayback implements Playback {

    private static final String TAG = LogHelper.makeLogTag(LocalPlayback.class);

    public static final float VOLUME_DUCK = 0.2f;//失去音频焦点时，小声播放
    public static final float VOLUME_NORMAL = 1.0f;//正常播放音量
    private static final int AUDIO_NO_FOCUS_NO_DUCK = 0;//没有音频焦点也不能小声播放的情况
    private static final int AUDIO_NO_FOCUS_CAN_DUCK = 1;//没有音频焦点，但是能低音量播放
    private static final int AUDIO_FOCUSED = 2;//有所有的音频焦点

    private final Context mContext;
    private int mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
    private final MusicProvider mMusicProvider;
    private SimpleExoPlayer mExoPlayer;
    private ExoPlayerEventListener mEventListener = new ExoPlayerEventListener();
    private boolean mExoPlayerNullIsStopped = false;
    private boolean mPlayOnFocusGain;//当前是否获取到音频焦点

    private final AudioManager mAudioManager;
    private final WifiManager.WifiLock mWifiLock;//持有WiFi锁，阻止系统关闭WiFi
    private boolean mAudioNoisyReceiverRegistered;//音频输出通道变化广播是否注册

    public LocalPlayback(Context context, MusicProvider musicProvider) {
        Context applicationContext = context.getApplicationContext();
        this.mContext = applicationContext;
        this.mMusicProvider = musicProvider;

        this.mAudioManager = (AudioManager) applicationContext.getSystemService(Context.AUDIO_SERVICE);
        this.mWifiLock = ((WifiManager) applicationContext.getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "heyzqt_lock");
    }

    private IntentFilter mAudioNoisyIntentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private BroadcastReceiver mAudioNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //音频输出焦点变化，耳机拔插、蓝牙耳机断开、Audio Output变化
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                LogHelper.d(TAG, "Audio outputs changed.");
                if (isPlaying()) {
                    //暂停音乐
                    pause();
                }
            }
        }
    };

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        giveUpAudioFocus();
        unregisterAudioNoisyReceiver();
        releaseResources(false);
    }

    @Override
    public boolean isPlaying() {
        return mPlayOnFocusGain || (mExoPlayer != null && mExoPlayer.getPlayWhenReady());
    }

    @Override
    public long getCurrentStreamPosition() {
        return 0;
    }

    @Override
    public void play() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void setState(int state) {

    }

    @Override
    public int getState() {
        if (mExoPlayer == null) {
            return mExoPlayerNullIsStopped ? PlaybackStateCompat.STATE_STOPPED
                    : PlaybackStateCompat.STATE_NONE;
        }

        switch (mExoPlayer.getPlaybackState()) {
            case Player.STATE_IDLE:
                return PlaybackStateCompat.STATE_PAUSED;
            case Player.STATE_BUFFERING:
                return PlaybackStateCompat.STATE_BUFFERING;
            case Player.STATE_READY:
                return mExoPlayer.getPlayWhenReady() ? PlaybackStateCompat.STATE_PLAYING
                        : PlaybackStateCompat.STATE_PAUSED;
            case Player.STATE_ENDED:
                return PlaybackStateCompat.STATE_PAUSED;
            default:
                return PlaybackStateCompat.STATE_NONE;
        }
    }

    @Override
    public void seekTo(long position) {

    }

    @Override
    public void setCurrentMediaId(String mediaId) {

    }

    @Override
    public String getCurrentMediaId() {
        return null;
    }

    @Override
    public void setCallback(Callback callback) {

    }

    //放弃音频焦点占用
    private void giveUpAudioFocus() {
        if (mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
        }
    }

    private final AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int focusChange) {

                }
            };

    private void releaseResources(boolean releasePlayer) {
        LogHelper.d(TAG, "releaseResources. releasePlayer=", releasePlayer);

        if (releasePlayer && mExoPlayer != null) {
            mExoPlayer.release();
            mExoPlayer.removeListener(mEventListener);
            mExoPlayer = null;
            mExoPlayerNullIsStopped = true;
            mPlayOnFocusGain = false;
        }

        if (mWifiLock.isHeld()) {
            mWifiLock.release();
        }
    }

    private void registerAudioNoisyReceiver() {
        if (!mAudioNoisyReceiverRegistered) {
            mContext.registerReceiver(mAudioNoisyReceiver, mAudioNoisyIntentFilter);
            mAudioNoisyReceiverRegistered = true;
        }
    }

    private void unregisterAudioNoisyReceiver() {
        if (mAudioNoisyReceiverRegistered) {
            mContext.unregisterReceiver(mAudioNoisyReceiver);
            mAudioNoisyReceiverRegistered = false;
        }
    }

    private class ExoPlayerEventListener implements Player.EventListener {
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            // TODO: 2019-08-19
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            // TODO: 2019-08-19
        }
    }
}
