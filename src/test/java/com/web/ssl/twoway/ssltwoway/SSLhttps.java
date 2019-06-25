package com.web.ssl.twoway.ssltwoway;

import com.web.ssl.twoway.ssltwoway.Constants;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import com.web.ssl.twoway.ssltwoway.Constants;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class SSLhttps {


    public static void main(String[] args) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream(new File(Constants.clientPrivateFile)), Constants.clientPrivatePassword.toCharArray());
        SSLContext sslcontext = SSLContexts.custom()
                //忽略掉对服务器端证书的校验
                .loadTrustMaterial(new TrustStrategy() {
                    public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        return true;
                    }
                })

                //加载服务端提供的truststore(如果服务器提供truststore的话就不用忽略对服务器端证书的校验了)
                .loadTrustMaterial(new File(Constants.trustAcFile), Constants.trustAcFilePassword.toCharArray(),
                        new TrustSelfSignedStrategy())
                .loadKeyMaterial(keyStore, Constants.clientPrivatePassword.toCharArray())
                .build();
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
                sslcontext,
                new String[]{"TLSv1"},
                null,
                SSLConnectionSocketFactory.getDefaultHostnameVerifier());
        CloseableHttpClient httpclient = HttpClients.custom()
                .setSSLSocketFactory(sslConnectionSocketFactory)//取消改行注释不导入运行环境的客户端证书 AAA
                .build();

        try {

            HttpGet httpget = new HttpGet("https://localhost:8443/");
//            若注释 AAA 行的内容，请求需要双向认证的服务会出现以下异常
//unable to find valid certification path to requested target
            System.out.println("Executing request " + httpget.getRequestLine());

            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                HttpEntity entity = response.getEntity();
                System.out.println(response.getStatusLine());
                System.out.println(IOUtils.toString(entity.getContent()));
                EntityUtils.consume(entity);
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }
}