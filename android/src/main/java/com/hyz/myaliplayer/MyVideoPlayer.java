package com.hyz.myaliplayer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.Surface;

import com.alivc.player.AliyunErrorCode;
import com.aliyun.vodplayer.media.AliyunLocalSource;
import com.aliyun.vodplayer.media.AliyunMediaInfo;
import com.aliyun.vodplayer.media.AliyunPlayAuth;
import com.aliyun.vodplayer.media.AliyunVidSts;
import com.aliyun.vodplayer.media.AliyunVodPlayer;
import com.aliyun.vodplayer.media.IAliyunVodPlayer;
import com.aliyun.vodplayerview.utils.NetWatchdog;

import io.flutter.plugin.common.EventChannel;
import io.flutter.view.TextureRegistry;

/**
 * Created by Huang yu zhao on 2018/7/25 0025.
 */

public class MyVideoPlayer {
    //播放器
    private AliyunVodPlayer mAliyunVodPlayer;
    //媒体信息
    private AliyunMediaInfo mAliyunMediaInfo;
    //整体缓冲进度
    private int mCurrentBufferPercentage = 0;
    private Surface surface;
    private Context context;
    private TextureRegistry.SurfaceTextureEntry textureEntry;
    public EventChannel.EventSink eventSink;
    private final EventChannel eventChannel;
    //网络状态监听
    private NetWatchdog mNetWatchdog;

    //对外的各种事件监听
    private IAliyunVodPlayer.OnInfoListener mOutInfoListener = null;
    private IAliyunVodPlayer.OnErrorListener mOutErrorListener = null;
    private IAliyunVodPlayer.OnRePlayListener mOutRePlayListener = null;
    private IAliyunVodPlayer.OnPcmDataListener mOutPcmDataListener = null;
    private IAliyunVodPlayer.OnAutoPlayListener mOutAutoPlayListener = null;
    private IAliyunVodPlayer.OnPreparedListener mOutPreparedListener = null;
    private IAliyunVodPlayer.OnCompletionListener mOutCompletionListener = null;
    private IAliyunVodPlayer.OnSeekCompleteListener mOuterSeekCompleteListener = null;
    private IAliyunVodPlayer.OnChangeQualityListener mOutChangeQualityListener = null;
    private IAliyunVodPlayer.OnFirstFrameStartListener mOutFirstFrameStartListener = null;
    private IAliyunVodPlayer.OnTimeExpiredErrorListener mOutTimeExpiredErrorListener = null;
    private IAliyunVodPlayer.OnUrlTimeExpiredListener mOutUrlTimeExpiredListener = null;

    // 连网断网监听
    private NetWatchdog.NetConnectedListener mNetConnectedListener = null;
    private NetWatchdog.NetChangeListener mNetChangeListener = null;

    //目前支持的几种播放方式
    private AliyunPlayAuth mAliyunPlayAuth;
    private AliyunLocalSource mAliyunLocalSource;
    private AliyunVidSts mAliyunVidSts;
    MyVideoPlayer(
            Context context,
            EventChannel eventChannel,
            TextureRegistry.SurfaceTextureEntry textureEntry) {
        this.context = context;
        this.eventChannel = eventChannel;
        this.textureEntry = textureEntry;

        mAliyunVodPlayer = new AliyunVodPlayer(this.context);
        registerListener();
        initNetWatchdog();
    }

    /**
     * 设置准备事件监听
     *
     * @param onPreparedListener 准备事件
     */
    public void setOnPreparedListener(IAliyunVodPlayer.OnPreparedListener onPreparedListener) {
        mOutPreparedListener = onPreparedListener;
    }

    /**
     * 设置错误事件监听
     *
     * @param onErrorListener 错误事件监听
     */
    public void setOnErrorListener(IAliyunVodPlayer.OnErrorListener onErrorListener) {
        mOutErrorListener = onErrorListener;
    }

    /**
     * 设置信息事件监听
     *
     * @param onInfoListener 信息事件监听
     */
    public void setOnInfoListener(IAliyunVodPlayer.OnInfoListener onInfoListener) {
        mOutInfoListener = onInfoListener;
    }

    /**
     * 设置播放完成事件监听
     *
     * @param onCompletionListener 播放完成事件监听
     */
    public void setOnCompletionListener(IAliyunVodPlayer.OnCompletionListener onCompletionListener) {
        mOutCompletionListener = onCompletionListener;
    }

    /**
     * 设置改变清晰度事件监听
     *
     * @param l 清晰度事件监听
     */
    public void setOnChangeQualityListener(IAliyunVodPlayer.OnChangeQualityListener l) {
        mOutChangeQualityListener = l;
    }

    /**
     * 设置重播事件监听
     *
     * @param onRePlayListener 重播事件监听
     */
    public void setOnRePlayListener(IAliyunVodPlayer.OnRePlayListener onRePlayListener) {
        mOutRePlayListener = onRePlayListener;
    }

    /**
     * 设置自动播放事件监听
     *
     * @param l 自动播放事件监听
     */
    public void setOnAutoPlayListener(IAliyunVodPlayer.OnAutoPlayListener l) {
        mOutAutoPlayListener = l;
    }

    /**
     * 设置PCM数据监听
     *
     * @param l PCM数据监听
     */
    public void setOnPcmDataListener(IAliyunVodPlayer.OnPcmDataListener l) {
        mOutPcmDataListener = l;
    }

    /**
     * 设置源超时监听
     *
     * @param l 源超时监听
     */
    public void setOnTimeExpiredErrorListener(IAliyunVodPlayer.OnTimeExpiredErrorListener l) {
        mOutTimeExpiredErrorListener = l;
    }

    /**
     * 设置鉴权过期监听，在鉴权过期前一分钟回调
     * @param listener
     */
    public void setOnUrlTimeExpiredListener(IAliyunVodPlayer.OnUrlTimeExpiredListener listener) {
        this.mOutUrlTimeExpiredListener = listener;
    }

    /**
     * 设置首帧显示事件监听
     *
     * @param onFirstFrameStartListener 首帧显示事件监听
     */
    public void setOnFirstFrameStartListener(IAliyunVodPlayer.OnFirstFrameStartListener onFirstFrameStartListener) {
        mOutFirstFrameStartListener = onFirstFrameStartListener;
    }

    /**
     * 设置seek结束监听
     *
     * @param onSeekCompleteListener seek结束监听
     */
    public void setOnSeekCompleteListener(IAliyunVodPlayer.OnSeekCompleteListener onSeekCompleteListener) {
        mOuterSeekCompleteListener = onSeekCompleteListener;
    }

    /**
     * 设置停止播放监听
     *
     * @param onStoppedListener 停止播放监听
     */
    public void setOnStoppedListener(IAliyunVodPlayer.OnStoppedListener onStoppedListener) {
        if (mAliyunVodPlayer != null) {
            mAliyunVodPlayer.setOnStoppedListner(onStoppedListener);
        }
    }

    /**
     * 设置加载状态监听
     *
     * @param onLoadingListener 加载状态监听
     */
    public void setOnLoadingListener(IAliyunVodPlayer.OnLoadingListener onLoadingListener) {
        if (mAliyunVodPlayer != null) {
            mAliyunVodPlayer.setOnLoadingListener(onLoadingListener);
        }
    }

    /**
     * 设置缓冲监听
     *
     * @param onBufferingUpdateListener 缓冲监听
     */
    public void setOnBufferingUpdateListener(IAliyunVodPlayer.OnBufferingUpdateListener onBufferingUpdateListener) {
        if (mAliyunVodPlayer != null) {
            mAliyunVodPlayer.setOnBufferingUpdateListener(onBufferingUpdateListener);
        }
    }

    /**
     * 设置视频宽高变化监听
     *
     * @param onVideoSizeChangedListener 视频宽高变化监听
     */
    public void setOnVideoSizeChangedListener(IAliyunVodPlayer.OnVideoSizeChangedListener onVideoSizeChangedListener) {
        if (mAliyunVodPlayer != null) {
            mAliyunVodPlayer.setOnVideoSizeChangedListener(onVideoSizeChangedListener);
        }
    }

    /**
     * 设置循环播放开始监听
     *
     * @param onCircleStartListener 循环播放开始监听
     */
    public void setOnCircleStartListener(IAliyunVodPlayer.OnCircleStartListener onCircleStartListener) {
        if (mAliyunVodPlayer != null) {
            mAliyunVodPlayer.setOnCircleStartListener(onCircleStartListener);
        }
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
        //设置准备回调
        mAliyunVodPlayer.setOnPreparedListener(new IAliyunVodPlayer.OnPreparedListener() {
            @Override
            public void onPrepared() {
                if (mAliyunVodPlayer == null) {
                    return;
                }

                mAliyunMediaInfo = mAliyunVodPlayer.getMediaInfo();
                if (mAliyunMediaInfo == null) {
                    return;
                }
                //防止服务器信息和实际不一致
                mAliyunMediaInfo.setDuration((int) mAliyunVodPlayer.getDuration());
                //使用用户设置的标题
                mAliyunMediaInfo.setTitle(getTitle(mAliyunMediaInfo.getTitle()));
                mAliyunMediaInfo.setPostUrl(getPostUrl(mAliyunMediaInfo.getPostUrl()));

                String postUrl = mAliyunMediaInfo.getPostUrl();
                //准备成功之后可以调用start方法开始播放
                if (mOutPreparedListener != null) {
                    mOutPreparedListener.onPrepared();
                }
            }
        });

        //播放器出错监听
        mAliyunVodPlayer.setOnErrorListener(new IAliyunVodPlayer.OnErrorListener() {
            @Override
            public void onError(int errorCode, int errorEvent, String errorMsg) {
            if (errorCode == AliyunErrorCode.ALIVC_ERR_INVALID_INPUTFILE.getCode()) {
                //当播放本地报错4003的时候，可能是文件地址不对，也有可能是没有权限。
                //如果是没有权限导致的，就做一个权限的错误提示。其他还是正常提示：
                int storagePermissionRet = ContextCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (storagePermissionRet != PackageManager.PERMISSION_GRANTED) {
                    errorMsg = AliyunErrorCode.ALIVC_ERR_NO_STORAGE_PERMISSION.getDescription(context);
                } else if (!NetWatchdog.hasNet(context)) {
                    //也可能是网络不行
                    errorCode = AliyunErrorCode.ALIVC_ERR_NO_NETWORK.getCode();
                    errorMsg = AliyunErrorCode.ALIVC_ERR_NO_NETWORK.getDescription(context);
                }
                if (mOutErrorListener != null) {
                    mOutErrorListener.onError(errorCode, errorEvent, errorMsg);
                }
            }
            }
        });
        //请求源过期信息
        mAliyunVodPlayer.setOnTimeExpiredErrorListener(new IAliyunVodPlayer.OnTimeExpiredErrorListener() {
            @Override
            public void onTimeExpiredError() {
                if (mOutTimeExpiredErrorListener != null) {
                    mOutTimeExpiredErrorListener.onTimeExpiredError();
                }
            }
        });
        //播放器加载回调
        mAliyunVodPlayer.setOnLoadingListener(new IAliyunVodPlayer.OnLoadingListener() {
            @Override
            public void onLoadStart() {

            }

            @Override
            public void onLoadEnd() {
            }

            @Override
            public void onLoadProgress(int percent) {
            }
        });
        //播放结束
        mAliyunVodPlayer.setOnCompletionListener(new IAliyunVodPlayer.OnCompletionListener() {
            @Override
            public void onCompletion() {
                if (mOutCompletionListener != null) {
                    mOutCompletionListener.onCompletion();
                }
            }
        });
        mAliyunVodPlayer.setOnBufferingUpdateListener(new IAliyunVodPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(int percent) {
                mCurrentBufferPercentage = percent;
            }
        });
        //播放信息监听
        mAliyunVodPlayer.setOnInfoListener(new IAliyunVodPlayer.OnInfoListener() {
            @Override
            public void onInfo(int arg0, int arg1) {
                if (mOutInfoListener != null) {
                    mOutInfoListener.onInfo(arg0, arg1);
                }
            }
        });
        //切换清晰度结果事件
        mAliyunVodPlayer.setOnChangeQualityListener(new IAliyunVodPlayer.OnChangeQualityListener() {
            @Override
            public void onChangeQualitySuccess(String finalQuality) {
                if (mOutChangeQualityListener != null) {
                    mOutChangeQualityListener.onChangeQualitySuccess(finalQuality);
                }
            }
            @Override
            public void onChangeQualityFail(int code, String msg) {
                if (code == CODE_SAME_QUALITY) {
                    if (mOutChangeQualityListener != null) {
                        mOutChangeQualityListener.onChangeQualitySuccess(mAliyunVodPlayer.getCurrentQuality());
                    }
                } else {
                    stop();
                    if (mOutChangeQualityListener != null) {
                        mOutChangeQualityListener.onChangeQualityFail(code, msg);
                    }
                }
            }
        });
        //重播监听
        mAliyunVodPlayer.setOnRePlayListener(new IAliyunVodPlayer.OnRePlayListener() {
            @Override
            public void onReplaySuccess() {
                if (mOutRePlayListener != null) {
                    mOutRePlayListener.onReplaySuccess();
                }
            }
        });
        //自动播放
        mAliyunVodPlayer.setOnAutoPlayListener(new IAliyunVodPlayer.OnAutoPlayListener() {
            @Override
            public void onAutoPlayStarted() {
                if (mOutAutoPlayListener != null) {
                    mOutAutoPlayListener.onAutoPlayStarted();
                }
            }
        });
        //seek结束事件
        mAliyunVodPlayer.setOnSeekCompleteListener(new IAliyunVodPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete() {
                if (mOuterSeekCompleteListener != null) {
                    mOuterSeekCompleteListener.onSeekComplete();
                }
            }
        });
        //PCM原始数据监听
        mAliyunVodPlayer.setOnPcmDataListener(new IAliyunVodPlayer.OnPcmDataListener() {
            @Override
            public void onPcmData(byte[] data, int size) {
                if (mOutPcmDataListener != null) {
                    mOutPcmDataListener.onPcmData(data, size);
                }
            }
        });
        //第一帧显示
        mAliyunVodPlayer.setOnFirstFrameStartListener(new IAliyunVodPlayer.OnFirstFrameStartListener() {
            @Override
            public void onFirstFrameStart() {
                if (mOutFirstFrameStartListener != null) {
                    mOutFirstFrameStartListener.onFirstFrameStart();
                }
            }
        });
        mAliyunVodPlayer.setOnUrlTimeExpiredListener(new IAliyunVodPlayer.OnUrlTimeExpiredListener() {
            @Override
            public void onUrlTimeExpired(String vid, String quality) {
                if (mOutUrlTimeExpiredListener!=null){
                    mOutUrlTimeExpiredListener.onUrlTimeExpired(vid, quality);
                }
            }
        });

    }

    void initSurface(final SurfaceTexture surfaceTexture){
        surface = new Surface(surfaceTexture);
        mAliyunVodPlayer.setSurface(surface);
    }

    /**
     * 初始化网络监听
     */
    private void initNetWatchdog() {
        mNetWatchdog = new NetWatchdog(context);
        mNetWatchdog.setNetChangeListener(new NetWatchdog.NetChangeListener(){
            @Override
            public void onWifiTo4G(){
                if(mNetChangeListener!=null)
                    mNetChangeListener.onWifiTo4G();
            }

            @Override
            public void on4GToWifi() {
                if(mNetChangeListener!=null)
                    mNetChangeListener.on4GToWifi();
            }

            @Override
            public void onNetDisconnected() {
                if(mNetChangeListener!=null)
                    mNetChangeListener.onNetDisconnected();
            }
        });
        mNetWatchdog.setNetConnectedListener(new NetWatchdog.NetConnectedListener(){
            @Override
            public void onReNetConnected(boolean isReconnect) {
                if (mNetConnectedListener != null) {
                    mNetConnectedListener.onReNetConnected(isReconnect);
                }
            }

            @Override
            public void onNetUnConnected() {
                if (mNetConnectedListener != null) {
                    mNetConnectedListener.onNetUnConnected();
                }
            }
        });
        mNetWatchdog.startWatch();

    }

    public void setNetConnectedListener (NetWatchdog.NetConnectedListener listener) {
        this.mNetConnectedListener = listener;
    }
    public void setNetConnectedListener (NetWatchdog.NetChangeListener listener) {
        this.mNetChangeListener = listener;
    }

    /**
     * 获取从源中设置的标题 。
     * 如果用户设置了标题，优先使用用户设置的标题。
     * 如果没有，就使用服务器返回的标题
     *
     * @param title 服务器返回的标题
     * @return 最后的标题
     */
    private String getTitle(String title) {
        String finalTitle = title;
        if (mAliyunLocalSource != null) {
            finalTitle = mAliyunLocalSource.getTitle();
        } else if (mAliyunPlayAuth != null) {
            finalTitle = mAliyunPlayAuth.getTitle();
        } else if (mAliyunVidSts != null) {
            finalTitle = mAliyunVidSts.getTitle();
        }

        if (TextUtils.isEmpty(finalTitle)) {
            return title;
        } else {
            return finalTitle;
        }
    }

    /**
     * 获取从源中设置的封面 。
     * 如果用户设置了封面，优先使用用户设置的封面。
     * 如果没有，就使用服务器返回的封面
     *
     * @param postUrl 服务器返回的封面
     * @return 最后的封面
     */
    private String getPostUrl(String postUrl) {
        String finalPostUrl = postUrl;
        if (mAliyunLocalSource != null) {
            finalPostUrl = mAliyunLocalSource.getCoverPath();
        } else if (mAliyunPlayAuth != null) {

        }

        if (TextUtils.isEmpty(finalPostUrl)) {
            return postUrl;
        } else {
            return finalPostUrl;
        }
    }


    /**
     * 获取整体缓冲进度
     *
     * @return 整体缓冲进度
     */
    public int getBufferPercentage() {
        if (mAliyunVodPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }


    /**
     * 获取视频时长
     *
     * @return 视频时长
     */
    public int getDuration() {
        if (mAliyunVodPlayer != null) {
            return (int) mAliyunVodPlayer.getDuration();
        }

        return 0;
    }

    public int getVideoWidth(){
        return mAliyunVodPlayer.getVideoWidth();
    }

    public int getVideoHeight(){
        return mAliyunVodPlayer.getVideoHeight();
    }

    /**
     * 清空之前设置的播放源
     */
    private void clearAllSource() {
        mAliyunPlayAuth = null;
        mAliyunVidSts = null;
        mAliyunLocalSource = null;
    }
    /**
     * 设置本地播放源
     *
     * @param aliyunLocalSource 本地播放源
     */
    private void setLocalSource(AliyunLocalSource aliyunLocalSource) {
        if (mAliyunVodPlayer == null) {
            return;
        }
        clearAllSource();
        stop();

        mAliyunLocalSource = aliyunLocalSource;
        if (NetWatchdog.is4GConnected(this.context)) {

        }
        prepareLocalSource(aliyunLocalSource);
    }
    /**
     * prepare本地播放源
     *
     * @param aliyunLocalSource 本地播放源
     */
    private void prepareLocalSource(AliyunLocalSource aliyunLocalSource) {
        mAliyunVodPlayer.prepareAsync(aliyunLocalSource);
    }

    /**
     * 准备vidsts源
     *
     * @param vidSts 源
     */
    private void setVidSts(AliyunVidSts vidSts) {
        if (mAliyunVodPlayer == null) {
            return;
        }

        clearAllSource();
        stop();


        mAliyunVidSts = vidSts;

        if (NetWatchdog.is4GConnected(this.context)) {

        }
        prepareVidsts(vidSts);
    }

    /**
     * 准备vidsts 源
     *
     * @param vidSts
     */
    private void prepareVidsts(AliyunVidSts vidSts) {

        mAliyunVodPlayer.prepareAsync(vidSts);
    }
    /**
     * 通过url播放
     * @param url
     */
    public void setPlaySource(String url){
        AliyunLocalSource.AliyunLocalSourceBuilder alsb = new AliyunLocalSource.AliyunLocalSourceBuilder();
        alsb.setSource(url);
        Uri uri = Uri.parse(url);
        if ("rtmp".equals(uri.getScheme())) {
            alsb.setTitle("");
        }
        AliyunLocalSource localSource = alsb.build();
        setLocalSource(localSource);
    }
    /**
     * 通过vid播放
     * @param vid
     * @param akId
     * @param akSecre
     * @param scuToken
     */
    public void setPlaySource(String vid,String akId,String akSecre,String scuToken){
        AliyunVidSts vidSts = new AliyunVidSts();
        vidSts.setVid(vid);
        vidSts.setAcId(akId);
        vidSts.setAkSceret(akSecre);
        vidSts.setSecurityToken(scuToken);
        setVidSts(vidSts);
    }

    void dispose(){
        if (mAliyunVodPlayer != null) {
            mAliyunVodPlayer.stop();
        }
        textureEntry.release();
        eventChannel.setStreamHandler(null);
        eventSink = null;
        if (surface != null) {
            surface.release();
        }
        if (mAliyunVodPlayer != null) {
            mAliyunVodPlayer.release();
        }
        if (mNetWatchdog != null) {
            mNetWatchdog.stopWatch();
        }
        mNetWatchdog = null;
    }
    /**
     * 设置循环播放
     *
     * @param circlePlay true:循环播放
     */
    public void setCirclePlay(boolean circlePlay) {
        if (mAliyunVodPlayer != null) {
            mAliyunVodPlayer.setCirclePlay(circlePlay);
        }
    }
    /**
     * 设置播放时的镜像模式
     *
     * @param mode 镜像模式
     */
    public void setRenderMirrorMode(IAliyunVodPlayer.VideoMirrorMode mode) {
        if (mAliyunVodPlayer != null) {
            mAliyunVodPlayer.setRenderMirrorMode(mode);
        }
    }

    /**
     * 设置播放时的旋转方向
     *
     * @param rotate 旋转角度
     */
    public void setRenderRotate(IAliyunVodPlayer.VideoRotate rotate) {
        if (mAliyunVodPlayer != null) {
            mAliyunVodPlayer.setRenderRotate(rotate);
        }
    }

    public void setCurrentVolume(int progress) {
        int bracketedValue = Math.max(0, Math.min(100, progress));
        mAliyunVodPlayer.setVolume(bracketedValue);
    }

    public int getCurrentVolume() {
        return mAliyunVodPlayer.getVolume();
    }

    public void setCurrentScreenBrigtness(int progress) {
        mAliyunVodPlayer.setScreenBrightness(progress);
    }

    public int getCurrentScreenBrigtness() {
        return mAliyunVodPlayer.getScreenBrightness();
    }
    void seekTo(int position){
        if (mAliyunVodPlayer == null) {
            return;
        }

        mAliyunVodPlayer.seekTo(position);
        mAliyunVodPlayer.start();
    }
    /**
     * 设置自动播放
     *
     * @param auto true 自动播放
     */
    public void setAutoPlay(boolean auto) {
        if (mAliyunVodPlayer != null) {
            mAliyunVodPlayer.setAutoPlay(auto);
        }
    }
    /**
     * 设置边播边存
     *
     * @param enable      是否开启。开启之后会根据maxDuration和maxSize决定有无缓存。
     * @param saveDir     保存目录
     * @param maxDuration 单个文件最大时长 秒
     * @param maxSize     所有文件最大大小 MB
     */
    public void setPlayingCache(boolean enable, String saveDir, int maxDuration, long maxSize) {
        if (mAliyunVodPlayer != null) {
            mAliyunVodPlayer.setPlayingCache(enable, saveDir, maxDuration, maxSize);
        }
    }
    void play(){
        if (mAliyunVodPlayer == null) {
            return;
        }

        IAliyunVodPlayer.PlayerState playerState = mAliyunVodPlayer.getPlayerState();
        if (playerState == IAliyunVodPlayer.PlayerState.Paused || playerState == IAliyunVodPlayer.PlayerState.Prepared || mAliyunVodPlayer.isPlaying()) {
            mAliyunVodPlayer.start();
        }
    }
    void pause(){
        if (mAliyunVodPlayer == null) {
            return;
        }

        IAliyunVodPlayer.PlayerState playerState = mAliyunVodPlayer.getPlayerState();
        if (playerState == IAliyunVodPlayer.PlayerState.Started || mAliyunVodPlayer.isPlaying()) {
            mAliyunVodPlayer.pause();
        }
    }

    /**
     * 停止播放
     */
    private void stop() {
        if (mAliyunVodPlayer != null) {
            mAliyunVodPlayer.stop();
        }
    }

    long getPosition(){
        long ret = mAliyunVodPlayer.getCurrentPosition();
        int duration = this.getDuration();
        int width = this.getVideoWidth();
        int height = this.getVideoHeight();
        return mAliyunVodPlayer.getCurrentPosition();
    }
}
