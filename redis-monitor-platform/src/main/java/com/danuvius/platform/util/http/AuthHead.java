package com.danuvius.platform.util.http;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 头部校验字段定义
 * <p>
 * userid在HTTP BODY里面，需要根据userid获取appkey，然后生成签名做校验；
 *
 * @author iPan
 * @date 2022-06-29
 */
public class AuthHead implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String HEAD_PATTERN = "^(AppId|Timestamp|Nonce|Signature)=\"([\\w\\-\\+/=]+)\"(,(AppId|Timestamp|Nonce|Signature)=\"([\\w\\-\\+/=]+)\")*$";
    static String HEAD = "OPEN-BODY-SIG";
    static String KEY = "Authorization";
    /**
     * APPID (长度32位)
     */
    private String appId = null;
    /**
     * 时间戳 (格式：yyyyMMddHHmmss)
     */
    private String timestamp = null;
    /**
     * 随机数 (长度不超过128位)
     */
    private String nonce = null;
    /**
     * 签名
     */
    private String signature = null;

    public AuthHead() {
    }

    public AuthHead(String appId, String timestamp, String nonce, String signature) {
        this.appId = appId;
        this.timestamp = timestamp;
        this.nonce = nonce;
        this.signature = signature;
    }
    public static String getHead() {return HEAD;}
    public static String getKey() {return KEY;}

    public static AuthHead parseHead(String headAuth) {
        Validate.notBlank(headAuth, "http token不能为空");
        if (!headAuth.startsWith(AuthHead.HEAD)) {
            throw new IllegalArgumentException("http token格式不正确");
        }

        String str = StringUtils.substringAfter(headAuth, AuthHead.HEAD).replaceAll("[\\s]", "");
        if (!Pattern.matches(HEAD_PATTERN, str)) {
            throw new IllegalArgumentException("http token格式不正确");
        }

        AuthHead obj;
        String appId, userId, timestamp, nonce, signature;
        try {
            String[] arr = str.split(",");
            Map<String, String> kv = new HashMap<String, String>();
            for (String item : arr) {
                int index = item.indexOf("=");
                String[] kvItem = new String[]{item.substring(0, index), item.substring(index + 1)};
                kv.put(kvItem[0], kvItem[1].replace("\"", ""));
            }
            appId = kv.get("AppId");
            timestamp = kv.get("Timestamp");
            nonce = kv.get("Nonce");
            signature = kv.get("Signature");
        } catch (Exception e) {
            throw new IllegalArgumentException("http token格式不正确");
        }
        if (StringUtils.isBlank(appId) || StringUtils.isBlank(timestamp)
                || StringUtils.isBlank(nonce) || StringUtils.isBlank(signature)) {
            throw new IllegalArgumentException("http token格式不正确");
        }
        try {
            ApiUtil.parseDate(timestamp);
        } catch (ParseException e) {
            throw new IllegalArgumentException("时间Timestamp格式不正确");
        }
        if (!StringUtils.isNumeric(nonce) || nonce.length() > 128) {
            throw new IllegalArgumentException("随机数Nonce格式不正确");
        }
        obj = new AuthHead();
        obj.setAppId(appId);
        obj.setTimestamp(timestamp);
        obj.setNonce(nonce);
        obj.setSignature(signature);
        return obj;
    }

    public String getAppId() {return appId;}
    public String getTimestamp() {return timestamp;}
    public String getNonce() {return nonce;}
    public String getSignature() {return signature;}

    public void setAppId(String appId) {this.appId = appId;}
    public void setTimestamp(String timestamp) {this.timestamp = timestamp;}
    public void setNonce(String nonce) {this.nonce = nonce;}
    public void setSignature(String signature) {this.signature = signature;}


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((appId == null) ? 0 : appId.hashCode());
        result = prime * result + ((nonce == null) ? 0 : nonce.hashCode());
        result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
        result = prime * result + ((signature == null) ? 0 : signature.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {return true;}
        if(obj == null){return false;}
        if(getClass() != obj.getClass()){return false;}
        AuthHead other = (AuthHead)obj;
        if(appId == null) {
            if(other.appId != null){
                return false;
            }
        }
        else if(!appId.equals(other.appId)){
            return false;
        }

        if(nonce == null) {
            if(other.nonce != null){
                return false;
            }
        }
        else if(!nonce.equals(other.nonce)){
            return false;
        }

        if(signature == null) {
            if(other.signature != null){
                return false;
            }
        }
        else if(!signature.equals(other.signature)){
            return false;
        }

        if(timestamp == null) {
            return other.timestamp == null;
        }else {
            return false;
        }
    }

    @Override
    public String toString() {
        return getKey()+":"+
                getHead()+" "+
                "AppId=\""+appId+"\"," +
                "Timestamp=\""+timestamp+"\"," +
                "Nonce=\""+nonce+"\"," +
                "Signature=\""+signature+"\"";
    }

    public String toHeadValue() {
        return getHead()+" "+
                "AppId=\""+appId+"\"," +
                "Timestamp=\""+timestamp+"\"," +
                "Nonce=\""+nonce+"\"," +
                "Signature=\""+signature+"\"";
    }
}
