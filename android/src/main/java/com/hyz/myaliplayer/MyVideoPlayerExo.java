package com.hyz.myaliplayer;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.view.Surface;

import com.aliyun.vodplayer.media.IAliyunVodPlayer;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.EventChannel;
import io.flutter.view.TextureRegistry;

import static com.google.android.exoplayer2.Player.REPEAT_MODE_ALL;
import static com.google.android.exoplayer2.Player.REPEAT_MODE_OFF;

/**
 * Created by Huang yu zhao on 2018/7/30 0030.
 */

public class MyVideoPlayerExo {
    private Surface surface;
    private Context context;
    private SimpleExoPlayer exoPlayer;
    public EventChannel.EventSink eventSink;
    private final EventChannel eventChannel;
    private boolean isInitialized = false;
    private TextureRegistry.SurfaceTextureEntry textureEntry;
    private IAliyunVodPlayer.OnPreparedListener mOutPreparedListener = null;
    public void setOnPreparedListener(IAliyunVodPlayer.OnPreparedListener onPreparedListener) {
        mOutPreparedListener = onPreparedListener;
    }
    MyVideoPlayerExo(
            Context context,
            EventChannel eventChannel,
            TextureRegistry.SurfaceTextureEntry textureEntry) {
        this.context = context;
        this.eventChannel = eventChannel;
        this.textureEntry = textureEntry;

        TrackSelector trackSelector = new DefaultTrackSelector();
        exoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
        registerListener();
    }
    private void registerListener() {
        eventChannel.setStreamHandler(
                new EventChannel.StreamHandler() {
                    @Override
                    public void onListen(Object o, EventChannel.EventSink sink) {
                        eventSink = sink;
                    }

                    @Override
                    public void onCancel(Object o) {
                        eventSink = null;
                    }
                }
        );

        setAudioAttributes(exoPlayer);

        exoPlayer.addListener(
                new Player.DefaultEventListener() {

                    @Override
                    public void onPlayerStateChanged(final boolean playWhenReady, final int playbackState) {
                        super.onPlayerStateChanged(playWhenReady, playbackState);
                        if (playbackState == Player.STATE_BUFFERING) {
                            if (eventSink != null) {
                                Map<String, Object> event = new HashMap<>();
                                event.put("event", "bufferingUpdate");
                                List<Integer> range = Arrays.asList(0, exoPlayer.getBufferedPercentage());
                                // iOS supports a list of buffered ranges, so here is a list with a single range.
                                event.put("values", Collections.singletonList(range));
                                eventSink.success(event);
                            }
                        } else if (playbackState == Player.STATE_READY && !isInitialized) {
                            isInitialized = true;
                            if (mOutPreparedListener != null) {
                                mOutPreparedListener.onPrepared();
                            }
                        }
                    }

                    @Override
                    public void onPlayerError(final ExoPlaybackException error) {
                        super.onPlayerError(error);
                        if (eventSink != null) {
                            eventSink.error("VideoError", "Video player had error " + error, null);
                        }
                    }
                });
    }

    private MediaSource buildMediaSource(Uri uri, DataSource.Factory mediaDataSourceFactory) {
        int type = Util.inferContentType(uri.getLastPathSegment());
        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource(
                        uri, null, new DefaultSsChunkSource.Factory(mediaDataSourceFactory), null, null);
            case C.TYPE_DASH:
                return new DashMediaSource(
                        uri, null, new DefaultDashChunkSource.Factory(mediaDataSourceFactory), null, null);
            case C.TYPE_HLS:
                return new HlsMediaSource(uri, mediaDataSourceFactory, null, null);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource(
                        uri, mediaDataSourceFactory, new DefaultExtractorsFactory(), null, null);
            default:
            {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }
    void initSurface(final SurfaceTexture surfaceTexture){
        surface = new Surface(surfaceTexture);
        exoPlayer.setVideoSurface(surface);
    }

    @SuppressWarnings("deprecation")
    private static void setAudioAttributes(SimpleExoPlayer exoPlayer) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            exoPlayer.setAudioAttributes(
                    new AudioAttributes.Builder().setContentType(C.CONTENT_TYPE_MOVIE).build());
        } else {
            exoPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
    }
    void dispose() {
        if (isInitialized) {
            exoPlayer.stop();
        }
        textureEntry.release();
        eventChannel.setStreamHandler(null);
        if (surface != null) {
            surface.release();
        }
        if (exoPlayer != null) {
            exoPlayer.release();
        }
    }

    public void setCirclePlay(boolean circlePlay) {
        if (exoPlayer != null) {
            exoPlayer.setRepeatMode(circlePlay ? REPEAT_MODE_ALL : REPEAT_MODE_OFF);
        }
    }
    public void setCurrentVolume(int progress) {
        int bracketedValue = Math.max(0, Math.min(100, progress));
        exoPlayer.setVolume(bracketedValue);
    }
    void seekTo(int location) {
        exoPlayer.seekTo(location);
    }

    long getPosition() {
        return exoPlayer.getCurrentPosition();
    }
    void play() {
        exoPlayer.setPlayWhenReady(true);
    }

    void pause() {
        exoPlayer.setPlayWhenReady(false);
    }
    public int getDuration() {
        if (exoPlayer != null) {
            return (int) exoPlayer.getDuration();
        }

        return 0;
    }

    public void setPlaySource(String vid,String akId,String akSecre,String scuToken){

    }
    public void setPlaySource(String url){
        Uri uri = Uri.parse(url);
        DataSource.Factory dataSourceFactory;
        dataSourceFactory =
                new DefaultHttpDataSourceFactory(
                        "ExoPlayer",
                        null,
                        DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                        DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
                        true);
        MediaSource mediaSource = buildMediaSource(uri, dataSourceFactory);
        exoPlayer.prepare(mediaSource);
    }
    public int getVideoWidth(){
        return exoPlayer.getVideoFormat().width;
    }

    public int getVideoHeight(){
        return exoPlayer.getVideoFormat().height;
    }
    public void setPlayingCache(boolean enable, String saveDir, int maxDuration, long maxSize) {

    }
    public void setOnFirstFrameStartListener(IAliyunVodPlayer.OnFirstFrameStartListener onFirstFrameStartListener) {
    }
}
