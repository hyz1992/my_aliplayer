package com.aliyun.vodplayerview.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.text.TextUtils;

import com.alivc.player.VcPlayerLog;
import com.aliyun.vodplayer.core.downloader.InfoSaveHelper;
import com.aliyun.vodplayer.downloader.AliyunDownloadMediaInfo;
import com.aliyun.vodplayer.downloader.AliyunDownloadMediaInfo.Status;

/**
 * @author Mulberry create on 2018/4/18.
 */

public class DownloadSaveInfoUtil {

    private static final String TAG = InfoSaveHelper.class.getSimpleName();
    private String mSaveDir;

    public DownloadSaveInfoUtil(String saveDir) {
        this.mSaveDir = saveDir;
    }

    public void writeDownloadingInfo(AliyunDownloadMediaInfo downloadInfo) {
        String savePath = downloadInfo.getSavePath();
        if (!TextUtils.isEmpty(savePath)) {
            File saveFile = new File(downloadInfo.getSavePath());
            VcPlayerLog.d("Downloader", "save file path :" + downloadInfo.getSavePath());
            VcPlayerLog.d("Downloader", "save info path dir :" + this.mSaveDir);
            VcPlayerLog.d("Downloader", "save info path  :" + getInfoFileName(saveFile.getName()));
            File infoFile = new File(this.mSaveDir, getInfoFileName(saveFile.getName()));
            if (!infoFile.exists() || infoFile.isDirectory()) {
                try {
                    infoFile.createNewFile();
                } catch (IOException var7) {
                    VcPlayerLog.e(TAG, "info 文件创建失败");
                }
            }

            List<AliyunDownloadMediaInfo> infos = new ArrayList();
            infos.add(downloadInfo);
            if (infoFile.exists()) {
                String content = AliyunDownloadMediaInfo.getJsonFromInfos(infos);
                VcPlayerLog.d("Downloader", "save content  :" + content);
                this.writeStringToFile(infoFile, content);
            }

        }
    }

    private static String getInfoFileName(String saveName) {
        return "." + saveName + ".info";
    }

    public List<AliyunDownloadMediaInfo> getAlivcDownloadeds() {
        if (mSaveDir != null) {

            File infoFile = new File(this.mSaveDir);
            if (infoFile.exists() && infoFile.isDirectory()) {
                File[] childs = infoFile.listFiles();
                if (childs == null) {
                    return null;
                } else {
                    List<AliyunDownloadMediaInfo> infos = new ArrayList();
                    File[] var4 = childs;
                    int var5 = childs.length;

                    for (int var6 = 0; var6 < var5; ++var6) {
                        File child = var4[var6];
                        if (child.getAbsolutePath().endsWith(".info")) {
                            String infoContent = this.readStringFromFile(child);
                            List<AliyunDownloadMediaInfo> infosFromJson = AliyunDownloadMediaInfo.getInfosFromJson(
                                infoContent);
                            AliyunDownloadMediaInfo info;
                            if (infosFromJson != null && !infosFromJson.isEmpty()) {
                                for (Iterator var10 = infosFromJson.iterator(); var10.hasNext(); infos.add(info)) {
                                    info = (AliyunDownloadMediaInfo)var10.next();
                                    if (info.getStatus() == Status.Complete) {
                                        info.setStatus(Status.Complete);
                                    }
                                }
                            }
                        }
                    }

                    return infos;
                }
            } else {
                return null;
            }
        }
        return null;
    }

    public void fillDownloadInfoFromCache(AliyunDownloadMediaInfo info) {
        String savePath = info.getSavePath();
        if (!TextUtils.isEmpty(savePath)) {
            File file = new File(savePath);
            String saveInfoName = getInfoFileName(file.getName());
            File infoFile = new File(this.mSaveDir, saveInfoName);
            String infoContent = this.readStringFromFile(infoFile);
            List<AliyunDownloadMediaInfo> infosFromJson = AliyunDownloadMediaInfo.getInfosFromJson(infoContent);
            if (infosFromJson != null && !infosFromJson.isEmpty()) {
                Iterator var8 = infosFromJson.iterator();

                while (var8.hasNext()) {
                    AliyunDownloadMediaInfo cacheInfo = (AliyunDownloadMediaInfo)var8.next();
                    info.setDownloadIndex(cacheInfo.getDownloadIndex());
                    info.setProgress(cacheInfo.getProgress());
                    Status cacheInfoStatus = cacheInfo.getStatus();
                    if (cacheInfoStatus == Status.Complete) {
                        info.setStatus(cacheInfoStatus);
                    }

                    info.setSavePath(cacheInfo.getSavePath());
                    info.setSize(Math.max(info.getSize(), cacheInfo.getSize()));
                }
            }

        }
    }

    public void deleteInfo(AliyunDownloadMediaInfo info) {
        deleteInfo(info, this.mSaveDir);
    }

    public void deleteAllInfo(ArrayList<AliyunDownloadMediaInfo> infos) {
        for (AliyunDownloadMediaInfo info : infos) {
            deleteInfo(info, this.mSaveDir);
        }
    }

    public static void deleteInfo(AliyunDownloadMediaInfo info, String saveDir) {
        if (info != null) {
            String savePath = info.getSavePath();
            if (!TextUtils.isEmpty(savePath)) {
                File file = new File(savePath);
                String saveInfoName = getInfoFileName(file.getName());
                File infoFile = new File(saveDir, saveInfoName);
                if (infoFile.exists() && !infoFile.isDirectory()) {
                    infoFile.delete();
                }
            }
        }
    }

    private void writeStringToFile(File file, String content) {
        PrintStream ps = null;

        try {
            ps = new PrintStream(new FileOutputStream(file));
            ps.println(content);
            ps.flush();
        } catch (FileNotFoundException var8) {
            VcPlayerLog.d(TAG, var8.getMessage());
        } finally {
            if (ps != null) {
                ps.close();
            }

        }

    }

    private String readStringFromFile(File file) {
        BufferedReader reader = null;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;

            while ((tempString = reader.readLine()) != null) {
                stringBuilder.append(tempString);
            }

            reader.close();
        } catch (IOException var13) {
            VcPlayerLog.d(TAG, var13.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException var12) {
                    ;
                }
            }

        }

        return stringBuilder.toString();
    }
}
