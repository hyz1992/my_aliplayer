package com.aliyun.vodplayerview.theme;

import com.aliyun.vodplayerview.view.tipsview.ErrorView;
import com.aliyun.vodplayerview.view.tipsview.NetChangeView;
import com.aliyun.vodplayerview.view.tipsview.ReplayView;
import com.aliyun.vodplayerview.widget.AliyunVodPlayerView;
import com.aliyun.vodplayerview.view.control.ControlView;
import com.aliyun.vodplayerview.view.guide.GuideView;
import com.aliyun.vodplayerview.view.quality.QualityView;
import com.aliyun.vodplayerview.view.speed.SpeedView;
import com.aliyun.vodplayerview.view.tipsview.TipsView;

/*
 * Copyright (C) 2010-2018 Alibaba Group Holding Limited.
 */

/**
 * 主题的接口。用于变换UI的主题。
 * 实现类有{@link ErrorView}，{@link NetChangeView} , {@link ReplayView} ,{@link ControlView},
 * {@link GuideView} , {@link QualityView}, {@link SpeedView} , {@link TipsView},
 * {@link AliyunVodPlayerView}
 */

public interface ITheme {
    /**
     * 设置主题
     * @param theme 支持的主题
     */
    void setTheme(AliyunVodPlayerView.Theme theme);
}
