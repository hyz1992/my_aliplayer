package com.aliyun.vodplayerview.view.download;

import java.util.ArrayList;

import com.aliyun.vodplayer.core.downloader.bean.DownloadMediaInfo;
import com.aliyun.vodplayer.downloader.AliyunDownloadMediaInfo;

/**
 * @author Mulberry
 *         create on 2018/4/12.
 */

public class AlivcDownloadMediaInfo{

    private boolean isEditState;
    private boolean isCheckedState;

    private AliyunDownloadMediaInfo aliyunDownloadMediaInfo;

    public boolean isEditState() {
        return isEditState;
    }

    public void setEditState(boolean editState) {
        isEditState = editState;
    }

    public boolean isCheckedState() {
        return isCheckedState;
    }

    public void setCheckedState(boolean checkedState) {
        isCheckedState = checkedState;
    }

    public AliyunDownloadMediaInfo getAliyunDownloadMediaInfo() {
        return aliyunDownloadMediaInfo;
    }

    public void setAliyunDownloadMediaInfo(AliyunDownloadMediaInfo aliyunDownloadMediaInfo) {
        this.aliyunDownloadMediaInfo = aliyunDownloadMediaInfo;
    }
}
