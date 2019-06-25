package com.web.ssl.twoway.ssltwoway;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import com.sun.net.httpserver.*;

import javax.net.ssl.*;

public class MyHTTPServer {
    public static void main(String[] args) {
        try {
            //实现HTTP SERVER
//            HttpServer hs = HttpServer.create(new InetSocketAddress(8443), 0);// 设置HttpServer的端口
//            hs.createContext("/", new MyHandler());// 用MyHandler类内处理到/的请求
//            hs.setExecutor(null); // creates a default executor
//            hs.start();

            //实现HTTPS SERVER
            HttpsServer httpsServer = HttpsServer.create(new InetSocketAddress(Constants.DEFAULT_PORT), 0);     //设置HTTPS端口这
            KeyStore ks = KeyStore.getInstance("JKS");   //建立证书库
            ks.load(new FileInputStream(Constants.serverPrivateFile), Constants.serverPrivatePassword.toCharArray());     //载入证书
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");     //建立一个密钥管理工厂
            kmf.init(ks, Constants.serverPrivatePassword.toCharArray());     //初始工厂

            KeyStore serverTrustKeyStore = KeyStore.getInstance("JKS");
            serverTrustKeyStore.load(new FileInputStream(Constants.trustAcFile), Constants.trustAcFilePassword.toCharArray());
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(serverTrustKeyStore);
            SavingTrustManager tm = new SavingTrustManager((X509TrustManager) trustManagerFactory.getTrustManagers()[0]);

            SSLContext sslContext = SSLContext.getInstance("TLS");     //建立证书实体
            sslContext.init(kmf.getKeyManagers(), new TrustManager[]{tm}, null);     //初始化证书

            httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters params) {
                    try {
                        // initialise the SSL context
                        SSLEngine engine = sslContext.createSSLEngine();
                        engine.setNeedClientAuth(true);
                        engine.setWantClientAuth(true);
                        params.setWantClientAuth(true);
                        params.setNeedClientAuth(true);
                        params.setCipherSuites(engine.getEnabledCipherSuites());
                        params.setProtocols(engine.getEnabledProtocols());
                        // get the default parameters
                        SSLParameters defaultSSLParameters = sslContext.getDefaultSSLParameters();
                        params.setSSLParameters(defaultSSLParameters);
                    } catch (Exception ex) {
                        System.out.println("Failed to create HTTPS port");
                    }
                }
            });
            // get the default parameters
//            SSLParameters defaultSSLParameters = c.getDefaultSSLParameters ();
//            httpsParameters.setSSLParameters ( defaultSSLParameters );
//            conf.configure(httpsParameters);
//            httpsServer.setHttpsConfigurator(conf);     //在https server载入配置
//            httpsServer.setExecutor(Constants.httpExecutor); // creates a default executor
            httpsServer.createContext("/", new MyHandler());// 用MyHandler类内处理到/的请求
            httpsServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class SavingTrustManager implements X509TrustManager {

    private final X509TrustManager tm;
    private X509Certificate[] chain;

    SavingTrustManager(X509TrustManager tm) {
        this.tm = tm;
    }

    public X509Certificate[] getAcceptedIssuers() {
        throw new UnsupportedOperationException();
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        this.chain = chain;
        tm.checkServerTrusted(chain, authType);
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        this.chain = chain;
        tm.checkServerTrusted(chain, authType);
    }
}

class MyHandler implements HttpHandler {


    public void handle(HttpExchange t) throws IOException {
        InputStream is = t.getRequestBody();
        System.out.println("response----");
        String response = "<font color='#ff0000'>come on baby</font>";
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}