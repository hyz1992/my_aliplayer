package com.hyz.testActivity;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;

import com.aliyun.vodplayer.media.AliyunLocalSource;
import com.aliyun.vodplayer.media.AliyunVodPlayer;
import com.aliyun.vodplayerview.utils.NetWatchdog;

/**
 * Created by Administrator on 2018/7/27 0027.
 */

public class TestLayout extends RelativeLayout {
    private SurfaceView mSurfaceView;
    private AliyunVodPlayer mAliyunVodPlayer;
    TestLayout(Context context){
        super(context);
        this.initSurfaceView();
        this.initAliVcPlayer();
    }
    private void initSurfaceView() {
        mSurfaceView = new SurfaceView(getContext().getApplicationContext());
        addSubView(mSurfaceView);

        SurfaceHolder holder = mSurfaceView.getHolder();
        //增加surfaceView的监听
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                mAliyunVodPlayer.setSurface(surfaceHolder.getSurface());
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width,
                                       int height) {
                mAliyunVodPlayer.surfaceChanged();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            }
        });
    }
    private void initAliVcPlayer() {
        mAliyunVodPlayer = new AliyunVodPlayer(getContext());
        mAliyunVodPlayer.setSurface(mSurfaceView.getHolder().getSurface());
    }
    public void setAutoPlay(boolean auto){
        if (mAliyunVodPlayer != null) {
            mAliyunVodPlayer.setAutoPlay(auto);
        }
    }
    void dispose(){
        if (mAliyunVodPlayer != null) {
            mAliyunVodPlayer.stop();
        }
    }
    private void addSubView(View view) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        addView(view, params);//添加到布局中
    }
    public void setLocalSource(AliyunLocalSource aliyunLocalSource) {
        if (mAliyunVodPlayer == null) {
            return;
        }
        stop();
        mAliyunVodPlayer.prepareAsync(aliyunLocalSource);

    }
    private void stop() {
        if (mAliyunVodPlayer != null) {
            mAliyunVodPlayer.stop();
        }
    }

}
