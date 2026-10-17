package com.danuvius.platform.util.http;

import cn.hutool.json.JSONObject;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.danuvius.platform.common.constant.CommonConstant;
import org.apache.commons.lang3.Validate;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

public class HttpUtil {
    public static JSONObject wrapData(JSONObject encryptedData){
        JSONObject json = new JSONObject();
        json.put("data", encryptedData);
        return json;
    }
    public static boolean handleExcludeURL(HttpServletRequest request) {
        if(CollectionUtils.isEmpty(CommonConstant.WHITE_URL_LIST)){return false;}

        String url = request.getServletPath();
        for(String pattern : CommonConstant.WHITE_URL_LIST){
            if(url.startsWith(pattern)){return true;}
        }
        return false;
    }

    public static boolean handleExcludeURL(String url) {
        if(CollectionUtils.isEmpty(CommonConstant.WHITE_URL_LIST)){return false;}

        for(String pattern : CommonConstant.WHITE_URL_LIST){
            if(url.startsWith(pattern)){return true;}
        }
        return false;
    }

    public static String postForSig(String uri, String jsonStr, AuthHead authHead, boolean encryptModel, String appkey) {
        Validate.notBlank(uri);
        Validate.notBlank(jsonStr);
        Validate.notNull(authHead);
//        String encryptJson = (encryptModel) ? wrapData(ApiUtil.encrypt(jsonStr, appkey)).toString() : jsonStr; // 模式：密文、明文
         String encryptJson = (encryptModel) ? JSON.toJSONString(ApiUtil.encrypt(jsonStr, appkey)) : jsonStr; // 模式：密文、明文
        URI uriObj = URI.create(uri);
        CloseableHttpClient httpclient = null;
        if ("https".equals(uriObj.getScheme())) {
            try {
                SSLContext sslContext = new org.apache.http.ssl.SSLContextBuilder().loadTrustMaterial((chain, authType) -> true).build();
                HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
                //设置连接超时时间，防止进程锁死
                httpclient = HttpClients.custom()
                        .setSSLSocketFactory(sslsf)
                        .setConnectionTimeToLive(60, TimeUnit.SECONDS).build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            httpclient = HttpClients.custom()
                    .setConnectionTimeToLive(60, TimeUnit.SECONDS).build();
        }
        HttpPost httppost = new HttpPost(uriObj);
        //添加http头信息
        httppost.addHeader(AuthHead.getKey(), authHead.toHeadValue());
        httppost.addHeader("Content-Type", "application/json;charset=UTF-8");
        httppost.addHeader("Accept", "application/json");
        httppost.addHeader("Charset", "UTF-8");
        StringEntity se = new StringEntity(encryptJson, "UTF-8");
        se.setContentType("application/json");
        se.setContentEncoding("UTF-8");
        httppost.setEntity(se);
        CloseableHttpResponse response = null;
        String respStr = null;
        try {
            response = httpclient.execute(httppost);
            respStr = EntityUtils.toString(response.getEntity(), "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //关闭连接
            if (response != null) {
                try {
                    response.close();
                } catch (IOException ignored) {
                }
            }
            if (httpclient != null) {
                try {
                    response.close();
                } catch (IOException ignored) {
                }
            }

        }
        return respStr;
    }


}
