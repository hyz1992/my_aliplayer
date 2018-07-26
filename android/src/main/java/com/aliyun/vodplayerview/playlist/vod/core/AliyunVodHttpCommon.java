package com.aliyun.vodplayerview.playlist.vod.core;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.UUID;

/**
 * Created by Mulberry on 2017/11/2.
 */

public class AliyunVodHttpCommon {

    public static final String VOD_DOMAIN = "https://vod.cn-shanghai.aliyuncs.com/";
    public static final String  HTTP_METHOD = "GET";

    public static class Action{
        public static final String CREATE_UPLOAD_IMAGE = "CreateUploadImage";
        public static final String CREATE_UPLOAD_VIDEO = "CreateUploadVideo";
        public static final String REFRESH_UPLOAD_VIDEO = "RefreshUploadVideo";
        public static final String GET_VIDEO_LIST = "GetVideoList";
    }

    public static class Status{
        public static final String NORMAL = "Normal";
    }

    public  static class ImageType{
        public static final String IMAGETYPE_COVER = "cover";
        public static final String IMAGETYPE_WATERMARK = "watermark";
    }

    public static class ImageExt{
        public static final String IMAGEEXT_PNG = "png";
        public static final String IMAGEEXT_JPG = "jpg";
        public static final String IMAGEEXT_JPEG = "jpeg";
    }

    public static class Format{
        public static final String FORMAT_JSON = "json";
        public static final String FORMAT_XML = "xml";
    }

    public static class CateId {
        // 推荐视频分类, 便于演示的视频,  值固定写死 
        public static final String CATEID = "34015820";
    }

    public static final String COMMON_API_VERSION = "2017-03-21";
    public static final String COMMON_TIMESTAMP = generateTimestamp();

    public static final String COMMON_SIGNATURE = "HMAC-SHA1";
    public static final String COMMON_SIGNATURE_METHOD = "HMAC-SHA1";
    public static final String COMMON_SIGNATUREVERSION = "1.0";
    public static final String COMMON_SIGNATURE_NONCE = generateRandom();
    public static final String COMON_NO_TRANSCODEMODE = "NoTranscode";
    public static final String COMON_FAST_TRANSCODEMODE = "FastTranscode";


    /*生成当前UTC时间戳Time*/
    public static String generateTimestamp() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(new SimpleTimeZone(0, "GMT"));
        return df.format(date);
    }

    public static String generateRandom() {
        String signatureNonce = UUID.randomUUID().toString();
        return signatureNonce;
    }


}
