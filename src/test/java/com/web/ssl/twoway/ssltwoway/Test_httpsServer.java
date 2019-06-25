package com.web.ssl.twoway.ssltwoway;

import java.io.*;
import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;

public class Test_httpsServer {

    public static HttpsServer httpsServer;

    public static void main(String args[]) throws IOException {
        try {
            httpsServer = HttpsServer.create(new InetSocketAddress(Constants.DEFAULT_PORT), 100);
            httpsServer.setExecutor(Constants.httpExecutor);
            //如果采用http注释掉这一行
            setSSLContext(httpsServer);

            httpsServer.createContext("/", new HttpHandler() {
                public void handle(HttpExchange exchange) throws IOException {
                    String query = exchange.getRequestURI().getRawQuery();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    copy(exchange.getRequestBody(), baos);
                    String data = baos.toString();
                    System.out.println("received a new request.");
                    System.out.println("query:" + query);
                    System.out.println("data:" + data);

                    HashMap<String, String> parameters = new HashMap<String, String>();
                    if (query != null && !query.equals("")) {
                        String[] paras = query.split("\\&");
                        for (String para : paras) {
                            String[] array = para.split("\\=");
                            if (array.length == 2) {
                                parameters.put(array[0], array[1]);
                            }
                        }
                    }
                    System.out.println(parameters);
                    System.out.println();
                    exchange.sendResponseHeaders(200, 0);
                    OutputStream out = exchange.getResponseBody();
                    String response = "<html><body>hello,welcome to this place.</body></html>";
                    out.write(response.getBytes());
                    out.close();
                    exchange.close();
                }
            });
            httpsServer.start();
            System.out.println("start server sucessfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setSSLContext(HttpsServer httpsServer) throws Exception {

        KeyStore serverKeyStore = KeyStore.getInstance("PKCS12");
        serverKeyStore.load(new FileInputStream(Constants.serverPrivateFile), Constants.serverPrivatePassword.toCharArray());
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(serverKeyStore, Constants.serverPrivatePassword.toCharArray());

        KeyStore serverTrustKeyStore = KeyStore.getInstance("JKS");
        serverTrustKeyStore.load(new FileInputStream(Constants.trustAcFile), Constants.trustAcFilePassword.toCharArray());
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(serverTrustKeyStore);

        SSLContext sslcontext = SSLContext.getInstance("TLS");
        sslcontext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
        HttpsConfigurator httpsConfigurator = new HttpsConfigurator(sslcontext);
        httpsServer.setHttpsConfigurator(httpsConfigurator);
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[512];
        int n = -1;
        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }
    }
}