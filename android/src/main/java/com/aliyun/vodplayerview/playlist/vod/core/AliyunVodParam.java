package com.aliyun.vodplayerview.playlist.vod.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Mulberry on 2017/11/2.
 */
public class AliyunVodParam {

    /**
     * 生成视频点播OpenAPI:GetVideoList 的私有参数
     * 不同API需要修改此方法中的参数
     * @return
     */
    public static Map<String, String> generatePrivateParamtersToGetVideoList() {
        Map<String, String> privateParams = new HashMap<>(8);
        privateParams.put(AliyunVodKey.KEY_VOD_ACTION, AliyunVodHttpCommon.Action.GET_VIDEO_LIST);
        privateParams.put(AliyunVodKey.KEY_VOD_STATUS, AliyunVodHttpCommon.Status.NORMAL);
        privateParams.put(AliyunVodKey.KEY_VOD_CATEID, AliyunVodHttpCommon.CateId.CATEID);
        return privateParams;
    }


    /**
     * 生成视频点播OpenAPI:CreateUploadImage 的私有参数
     * 不同API需要修改此方法中的参数
     * @return
     */
    public static Map<String, String> generatePrivateParamtersToUploadImage() {
        Map<String, String> privateParams = new HashMap<>(8);
        privateParams.put(AliyunVodKey.KEY_VOD_ACTION, AliyunVodHttpCommon.Action.CREATE_UPLOAD_IMAGE);
        privateParams.put(AliyunVodKey.KEY_VOD_IMAGETYPE, AliyunVodHttpCommon.ImageType.IMAGETYPE_COVER);
        privateParams.put(AliyunVodKey.KEY_VOD_IMAGEEXT, AliyunVodHttpCommon.ImageExt.IMAGEEXT_PNG);
        return privateParams;
    }

    /**
     * 生成视频点播OpenAPI:RefreshUploadVideo 的私有参数
     * 不同API需要修改此方法中的参数
     * @return
     */
    public static Map<String, String> generatePrivateParamtersToReUploadVideo(String  videoId) {
        Map<String, String> privateParams = new HashMap<>(8);
        privateParams.put(AliyunVodKey.KEY_VOD_ACTION, AliyunVodHttpCommon.Action.REFRESH_UPLOAD_VIDEO);
        privateParams.put(AliyunVodKey.KEY_VOD_VIDEOID, videoId);
        return privateParams;
    }

    /**
     * 生成视频点播OpenAPI公共参数
     * 不需要修改
     * @return
     */
    public static Map<String, String> generatePublicParamters(String accessKeyId,String securityToken) {
        Map<String, String> publicParams = new HashMap<>(8);
        publicParams.put(AliyunVodKey.KEY_VOD_COMMON_FORMAT, AliyunVodHttpCommon.Format.FORMAT_JSON);
        publicParams.put(AliyunVodKey.KEY_VOD_COMMON_VERSION, AliyunVodHttpCommon.COMMON_API_VERSION);
        publicParams.put(AliyunVodKey.KEY_VOD_COMMON_ACCESSKEYID, accessKeyId);
        publicParams.put(AliyunVodKey.KEY_VOD_COMMON_SIGNATURE_METHOD, AliyunVodHttpCommon.COMMON_SIGNATURE_METHOD);
        publicParams.put(AliyunVodKey.KEY_VOD_COMMON_SIGNATURE_VERSION, AliyunVodHttpCommon.COMMON_SIGNATUREVERSION);
        publicParams.put(AliyunVodKey.KEY_VOD_COMMON_SIGNATURE_NONCE, AliyunVodHttpCommon.generateRandom());
        if (securityToken != null && securityToken.length() > 0) {
            publicParams.put(AliyunVodKey.KEY_VOD_COMMON_SECURITY_TOKEN, securityToken);
        }
        return publicParams;
    }

    /**
     * 生成OpenAPI地址
     * @param privateParams
     * @return
     * @throws Exception
     */
    public static String generateOpenAPIURL(Map<String, String> publicParams, Map<String, String> privateParams,String accessKeySecret) {
        return  generateURL(AliyunVodHttpCommon.VOD_DOMAIN, AliyunVodHttpCommon.HTTP_METHOD, publicParams, privateParams,accessKeySecret);
    }

    /**
     * @param domain        请求地址
     * @param httpMethod    HTTP请求方式GET，POST等
     * @param publicParams  公共参数
     * @param privateParams 接口的私有参数
     * @return 最后的url
     */
    private static String generateURL(String domain, String httpMethod, Map<String, String> publicParams, Map<String, String> privateParams,String accessKeySecret) {
        List<String> allEncodeParams = AliyunVodSignature.getAllParams(publicParams, privateParams);
        String cqsString = AliyunVodSignature.getCQS(allEncodeParams);
        System.out.print("CanonicalizedQueryString = " + cqsString);
        String stringToSign = httpMethod + "&" + AliyunVodSignature.percentEncode("/") + "&" + AliyunVodSignature.percentEncode(cqsString);
        System.out.print("StringtoSign = " + stringToSign);
        String signature = AliyunVodSignature.hmacSHA1Signature(accessKeySecret, stringToSign);
        System.out.print("Signature = " + signature);
        return domain + "?" + cqsString + "&" + AliyunVodSignature.percentEncode(AliyunVodKey.KEY_VOD_COMMON_SIGNATURE) + "=" + AliyunVodSignature.percentEncode(signature);
    }


    private static String generateTags(List<String> tags){
        String tag = "";
        if (tags==null || tags.size()<=0){
            return tag;
        }
        for (int i = 0; i < tags.size(); i++) {
            tag =tag + "," + tags.get(i).toString();
        }
            return trimFirstAndLastChar(tag, ',');
    }

    /**
     * 去除字符串首尾出现的某个字符.
     * @param source 源字符串.
     * @param element 需要去除的字符.
     * @return String.
     */
    public static String trimFirstAndLastChar(String source,char element){
        boolean beginIndexFlag = true;
        boolean endIndexFlag = true;
        do{
            int beginIndex = source.indexOf(element) == 0 ? 1 : 0;
            int endIndex = source.lastIndexOf(element) + 1 == source.length() ? source.lastIndexOf(element) : source.length();
            source = source.substring(beginIndex, endIndex);
            beginIndexFlag = (source.indexOf(element) == 0);
            endIndexFlag = (source.lastIndexOf(element) + 1 == source.length());
        } while (beginIndexFlag || endIndexFlag);
        return source;
    }
}
