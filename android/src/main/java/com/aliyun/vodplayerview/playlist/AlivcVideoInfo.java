package com.aliyun.vodplayerview.playlist;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * @author Mulberry create on 2018/5/17.
 */

public class AlivcVideoInfo {
    @SerializedName("VideoList")
    private VideoList videoList;
    @SerializedName("RequestId")
    private String requestId;
    @SerializedName("Total")
    private String total;

    public VideoList getVideoList() {
        return videoList;
    }

    public void setVideoList(VideoList videoList) {
        this.videoList = videoList;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "ClassPojo [VideoList = " + videoList + ", RequestId = " + requestId + ", Total = " + total + "]";
    }

    public static class VideoList {
        @SerializedName("Video")
        private ArrayList<Video> video;

        public ArrayList<AlivcVideoInfo.Video> getVideo() {
            return video;
        }

        public void setVideo(ArrayList<AlivcVideoInfo.Video> video) {
            this.video = video;
        }

        @Override
        public String toString() {
            return "ClassPojo [Video = " + video + "]";
        }
    }

    public static class Video {
        @SerializedName("CreationTime")
        private String creationTime;
        @SerializedName("CoverURL")
        private String coverURL;
        @SerializedName("Status")
        private String status;
        @SerializedName("VideoId")
        private String videoId;
        @SerializedName("Duration")
        private String duration;
        @SerializedName("CreateTime")
        private String createTime;
        @SerializedName("Snapshots")
        private Snapshots snapshots;
        @SerializedName("ModifyTime")
        private String modifyTime;
        @SerializedName("Title")
        private String title;
        @SerializedName("Size")
        private String size;
        @SerializedName("Description")
        private String description;
        @SerializedName("CateName")
        private String cateName;
        @SerializedName("CateId")
        private String cateId;

        public String getCateId() {
            return cateId;
        }

        public void setCateId(String cateId) {
            this.cateId = cateId;
        }

        public String getCateName() {
            return cateName;
        }

        public void setCateName(String cateName) {
            this.cateName = cateName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getCreationTime() {
            return createTime;
        }

        public void setCreationTime(String creationTime) {
            this.createTime = creationTime;
        }

        public String getCoverURL() {
            return coverURL;
        }

        public void setCoverURL(String coverURL) {
            this.coverURL = coverURL;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getVideoId() {
            return videoId;
        }

        public void setVideoId(String videoId) {
            this.videoId = videoId;
        }

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public String getCreateTime() {
            return creationTime;
        }

        public void setCreateTime(String creationTime) {
            this.creationTime = creationTime;
        }

        public Snapshots getSnapshots() {
            return snapshots;
        }

        public void setSnapshots(Snapshots snapshots) {
            this.snapshots = snapshots;
        }

        public String getModifyTime() {
            return modifyTime;
        }

        public void setModifyTime(String modifyTime) {
            this.modifyTime = modifyTime;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        @Override
        public String toString() {
            return "ClassPojo [CreationTime = " + creationTime + ", CoverURL = " + coverURL + ", Status = " + status
                + ", VideoId = " + videoId + ", Duration = " + duration + ", CreateTime = " + createTime
                + ", Snapshots = " + snapshots + ", ModifyTime = " + modifyTime + ", Title = " + title + ", Size = "
                + size + "]";
        }
    }

    private class Snapshots {
        private String[] snapshot;

        public String[] getSnapshot() {
            return snapshot;
        }

        public void setSnapshot(String[] snapshot) {
            this.snapshot = snapshot;
        }

        @Override
        public String toString() {
            return "ClassPojo [Snapshot = " + snapshot + "]";
        }
    }
}
