package com.web.ssl.twoway.ssltwoway;

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

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

public class SSLtcp {
    private static final String DEFAULT_HOST = "127.0.0.1";
    private SSLSocket sslSocket;

    public static void main(String[] args) throws Exception {
        SSLtcp client = new SSLtcp();
        client.init();
        client.process();
    }

    /**
     * 通过ssl socket与服务端进行连接,并且发送一个消息
     */
    public void process() {
        if (sslSocket == null) {
            System.out.println("ERROR");
            return;
        }
        try {
            InputStream input = sslSocket.getInputStream();
            OutputStream output = sslSocket.getOutputStream();
            BufferedInputStream bis = new BufferedInputStream(input);
            BufferedOutputStream bos = new BufferedOutputStream(output);
            bos.write("Client Message".getBytes());
            bos.flush();
            byte[] buffer = new byte[20];
            bis.read(buffer);
            System.out.println(new String(buffer));
            sslSocket.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    /**
     * <ul>
     * <li>ssl连接的重点:</li>
     * <li>初始化SSLSocket</li>
     * <li>导入客户端私钥KeyStore，导入客户端受信任的KeyStore(服务端的证书)</li>
     * </ul>
     */
    public void init() {
        try {
            SSLContext ctx = SSLContext.getInstance("SSL");
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            KeyStore ks = KeyStore.getInstance("PKCS12");
            KeyStore tks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(Constants.clientPrivateFile), Constants.clientPrivatePassword.toCharArray());
            tks.load(new FileInputStream(Constants.trustAcFile), Constants.trustAcFilePassword.toCharArray());
            kmf.init(ks, Constants.CLIENT_KEY_STORE_PASSWORD.toCharArray());
            tmf.init(tks);
            ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            sslSocket = (SSLSocket) ctx.getSocketFactory().createSocket(DEFAULT_HOST, Constants.DEFAULT_PORT);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
