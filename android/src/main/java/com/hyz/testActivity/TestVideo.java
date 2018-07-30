package com.hyz.testActivity;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.aliyun.vodplayer.media.AliyunLocalSource;
import com.aliyun.vodplayer.media.AliyunVidSts;
import com.aliyun.vodplayerview.constants.PlayParameter;

/**
 * Created by Administrator on 2018/7/27 0027.
 */

public class TestVideo extends Activity {
    TestLayout video;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        video = new TestLayout(this);
        setContentView(video);

        video.setAutoPlay(true);
        String url = "http://hxzhex.zstarpoker.com/sv/c3fa555-164b680cc94/c3fa555-164b680cc94.mp4";
        playUrl(url);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        video.dispose();
    }

    void playUrl(String url){
        PlayParameter.PLAY_PARAM_TYPE = "localSource";
        PlayParameter.PLAY_PARAM_URL = url;
        setPlaySource();
    }
    private void setPlaySource() {
        AliyunLocalSource.AliyunLocalSourceBuilder alsb = new AliyunLocalSource.AliyunLocalSourceBuilder();
        alsb.setSource(PlayParameter.PLAY_PARAM_URL);
        Uri uri = Uri.parse(PlayParameter.PLAY_PARAM_URL);
        if ("rtmp".equals(uri.getScheme())) {
            alsb.setTitle("");
        }
        AliyunLocalSource localSource = alsb.build();
        video.setLocalSource(localSource);
    }
}