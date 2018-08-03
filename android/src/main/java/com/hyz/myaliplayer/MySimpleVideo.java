package com.hyz.myaliplayer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.widget.Toast;

import com.aliyun.vodplayer.media.AliyunLocalSource;
import com.aliyun.vodplayer.media.AliyunVidSts;
import com.aliyun.vodplayerview.utils.VidStsUtil;
import com.aliyun.vodplayerview.widget.AliyunVodPlayerView;
import com.aliyun.vodplayer.media.IAliyunVodPlayer;

import java.lang.ref.WeakReference;

/**
 * Created by Huang yu zhao on 2018/7/24 0024.
 */

public class MySimpleVideo extends Activity {
    enum PlayType{
        url,
        sts,
    }
    private boolean inRequest = false;
    private AliyunVodPlayerView mAliyunVodPlayerView = null;
    private static final String DEFAULT_URL = "http://player.alicdn.com/video/aliyunmedia.mp4";
    private static final String DEFAULT_VID = "6e783360c811449d8692b2117acc9212";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initAliyunPlayerView();

        Intent i = getIntent();
        Integer playType = i.getIntExtra("playType",0);
        if(playType==PlayType.url.ordinal()){//根据url播放
            String url = "http://www.sample-videos.com/video/mp4/720/big_buck_bunny_720p_20mb.mp4";
            url = i.getStringExtra("url");
            setPlaySource(url);
        }else if(playType==PlayType.sts.ordinal()){//根据vidSts播放
            String vid = i.getStringExtra("vid");
            String akId = i.getStringExtra("akId");
            String akScere = i.getStringExtra("akScere");
            String scuToken = i.getStringExtra("scuToken");
            setPlaySource(vid,akId,akScere,scuToken);
        }else{
            finish();
        }
    }
    private void initAliyunPlayerView() {
        mAliyunVodPlayerView = new AliyunVodPlayerView(this);
        setContentView(mAliyunVodPlayerView);
        //保持屏幕敞亮
        mAliyunVodPlayerView.setKeepScreenOn(true);
        String sdDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test_save_cache";
        mAliyunVodPlayerView.setPlayingCache(false, sdDir, 60 * 60 /*时长, s */, 300 /*大小，MB*/);
        mAliyunVodPlayerView.setTheme(AliyunVodPlayerView.Theme.Blue);
        //mAliyunVodPlayerView.setCirclePlay(true);
        mAliyunVodPlayerView.setAutoPlay(true);
        mAliyunVodPlayerView.setOnPreparedListener(new MyPrepareListener(this));
    }

    /**
     * 请求sts
     * （根据videoId播放）
     */
    private void requestVidSts(String vid) {
        if (inRequest) {
            return;
        }
        inRequest = true;
        VidStsUtil.getVidSts(vid, new MyStsListener(this));
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
                activity.inRequest = false;
                activity.setPlaySource(vid, akid, akSecret, token);
            }
        }

        @Override
        public void onFail() {
            MySimpleVideo activity = weakctivity.get();
            if (activity != null) {
                activity.inRequest = false;
                activity.onStsFail();
            }
        }
    }

    private void onStsFail() {
        Toast.makeText(getApplicationContext(), "请求vidsts失败", Toast.LENGTH_LONG).show();
        finish();
    }

    private void setPlaySource(String url){
        AliyunLocalSource.AliyunLocalSourceBuilder alsb = new AliyunLocalSource.AliyunLocalSourceBuilder();
        alsb.setSource(url);
        Uri uri = Uri.parse(url);
        if ("rtmp".equals(uri.getScheme())) {
            alsb.setTitle("");
        }
        AliyunLocalSource localSource = alsb.build();
        mAliyunVodPlayerView.setLocalSource(localSource);
    }
    private void setPlaySource(String vid,String akId,String akSecre,String scuToken){
        AliyunVidSts vidSts = new AliyunVidSts();
        vidSts.setVid(vid);
        vidSts.setAcId(akId);
        vidSts.setAkSceret(akSecre);
        vidSts.setSecurityToken(scuToken);
        mAliyunVodPlayerView.setVidSts(vidSts);
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
