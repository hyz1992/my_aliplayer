package com.hyz.myaliplayer;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.widget.Toast;

import com.aliyun.vodplayer.media.AliyunLocalSource;
import com.aliyun.vodplayer.media.AliyunVidSts;
import com.aliyun.vodplayerview.constants.PlayParameter;
import com.aliyun.vodplayerview.utils.VidStsUtil;
import com.aliyun.vodplayerview.widget.AliyunVodPlayerView;
import com.aliyun.vodplayer.media.IAliyunVodPlayer;

import java.lang.ref.WeakReference;

/**
 * Created by Huang yu zhao on 2018/7/24 0024.
 */

public class MySimpleVideo extends Activity {
    private boolean inRequest = false;
    private AliyunVodPlayerView mAliyunVodPlayerView = null;
    private static final String DEFAULT_URL = "http://player.alicdn.com/video/aliyunmedia.mp4";
    private static final String DEFAULT_VID = "6e783360c811449d8692b2117acc9212";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_simple_video_layout);

        initAliyunPlayerView();
//        requestVidSts();
        String url = "http://www.sample-videos.com/video/mp4/720/big_buck_bunny_720p_20mb.mp4";
        url = "http://hxzhex.zstarpoker.com/sv/c3fa555-164b680cc94/c3fa555-164b680cc94.mp4";
        playUrl(url);
    }
    private void initAliyunPlayerView() {
        mAliyunVodPlayerView = (AliyunVodPlayerView)findViewById(R.id.my_video_view);
        //保持屏幕敞亮
        mAliyunVodPlayerView.setKeepScreenOn(true);
        PlayParameter.PLAY_PARAM_URL = DEFAULT_URL;
        String sdDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test_save_cache";
        mAliyunVodPlayerView.setPlayingCache(false, sdDir, 60 * 60 /*时长, s */, 300 /*大小，MB*/);
        mAliyunVodPlayerView.setTheme(AliyunVodPlayerView.Theme.Blue);
        //mAliyunVodPlayerView.setCirclePlay(true);
        mAliyunVodPlayerView.setAutoPlay(true);

        mAliyunVodPlayerView.setOnPreparedListener(new MyPrepareListener(this));

    }

    /**
     * 根据url播放
     * @param url
     */
    void playUrl(String url){
        PlayParameter.PLAY_PARAM_TYPE = "localSource";
        PlayParameter.PLAY_PARAM_URL = url;
        setPlaySource();
    }
    /**
     * 请求sts
     * （根据videoId播放）
     */
    private void requestVidSts() {
        if (inRequest) {
            return;
        }
        inRequest = true;
        PlayParameter.PLAY_PARAM_VID = DEFAULT_VID;
        VidStsUtil.getVidSts(PlayParameter.PLAY_PARAM_VID, new MyStsListener(this));
    }

    private static class MyStsListener implements VidStsUtil.OnStsResultListener {

        private WeakReference<MySimpleVideo> weakctivity;

        public MyStsListener(MySimpleVideo act) {
            weakctivity = new WeakReference<MySimpleVideo>(act);
        }

        @Override
        public void onSuccess(String vid, String akid, String akSecret, String token) {
            MySimpleVideo activity = weakctivity.get();
            if (activity != null) {
                activity.onStsSuccess(vid, akid, akSecret, token);
            }
        }

        @Override
        public void onFail() {
            MySimpleVideo activity = weakctivity.get();
            if (activity != null) {
                activity.onStsFail();
            }
        }
    }

    private void onStsFail() {

        Toast.makeText(getApplicationContext(), "请求vidsts失败", Toast.LENGTH_LONG).show();
        inRequest = false;
        //finish();
    }

    private void onStsSuccess(String mVid, String akid, String akSecret, String token) {

        PlayParameter.PLAY_PARAM_VID = mVid;
        PlayParameter.PLAY_PARAM_AK_ID = akid;
        PlayParameter.PLAY_PARAM_AK_SECRE = akSecret;
        PlayParameter.PLAY_PARAM_SCU_TOKEN = token;

        inRequest = false;
        // 请求sts成功后, 加载播放资源,和视频列表
        setPlaySource();
    }
    private void setPlaySource() {
        if ("localSource".equals(PlayParameter.PLAY_PARAM_TYPE)) {
            AliyunLocalSource.AliyunLocalSourceBuilder alsb = new AliyunLocalSource.AliyunLocalSourceBuilder();
            alsb.setSource(PlayParameter.PLAY_PARAM_URL);
            Uri uri = Uri.parse(PlayParameter.PLAY_PARAM_URL);
            if ("rtmp".equals(uri.getScheme())) {
                alsb.setTitle("");
            }
            AliyunLocalSource localSource = alsb.build();
            mAliyunVodPlayerView.setLocalSource(localSource);

        } else if ("vidsts".equals(PlayParameter.PLAY_PARAM_TYPE)) {
            if (!inRequest) {
                AliyunVidSts vidSts = new AliyunVidSts();
                vidSts.setVid(PlayParameter.PLAY_PARAM_VID);
                vidSts.setAcId(PlayParameter.PLAY_PARAM_AK_ID);
                vidSts.setAkSceret(PlayParameter.PLAY_PARAM_AK_SECRE);
                vidSts.setSecurityToken(PlayParameter.PLAY_PARAM_SCU_TOKEN);
                if (mAliyunVodPlayerView != null) {
                    mAliyunVodPlayerView.setVidSts(vidSts);
                }
            }
        }
    }

    private static class MyPrepareListener implements IAliyunVodPlayer.OnPreparedListener {

        private WeakReference<MySimpleVideo> activityWeakReference;

        public MyPrepareListener(MySimpleVideo skinActivity) {
            activityWeakReference = new WeakReference<MySimpleVideo>(skinActivity);
        }

        @Override
        public void onPrepared() {
            MySimpleVideo activity = activityWeakReference.get();
            if (activity != null) {
                activity.onPrepared();
            }
        }
    }
    void onPrepared(){

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAliyunVodPlayerView != null) {
            mAliyunVodPlayerView.onResume();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAliyunVodPlayerView != null) {
            mAliyunVodPlayerView.onStop();
        }
    }

    @Override
    protected void onDestroy() {
        if (mAliyunVodPlayerView != null) {
            mAliyunVodPlayerView.onDestroy();
            mAliyunVodPlayerView = null;
        }

        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mAliyunVodPlayerView != null) {
            boolean handler = mAliyunVodPlayerView.onKeyDown(keyCode, event);
            if (!handler) {
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
