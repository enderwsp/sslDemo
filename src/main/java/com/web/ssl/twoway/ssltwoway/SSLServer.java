package com.web.ssl.twoway.ssltwoway;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.Socket;
import java.security.KeyStore;

public class SSLServer {
    private SSLServerSocket serverSocket;

    public void start() {
        if (serverSocket == null) {
            System.out.println("ERROR");
            return;
        }
        while (true) {
            try {
                Socket s = serverSocket.accept();
                InputStream input = s.getInputStream();
                OutputStream output = s.getOutputStream();
                BufferedInputStream bis = new BufferedInputStream(input);
                BufferedOutputStream bos = new BufferedOutputStream(output);
                byte[] buffer = new byte[20];
                bis.read(buffer);
                System.out.println(new String(buffer));
                bos.write("Server Echo".getBytes());
                bos.flush();
                s.close();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    public void init() {
        try {
            SSLContext ctx = SSLContext.getInstance("SSL");
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            KeyStore ks = KeyStore.getInstance("PKCS12");
            KeyStore tks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(Constants.serverPrivateFile), Constants.serverPrivatePassword.toCharArray());
            tks.load(new FileInputStream(Constants.trustAcFile), Constants.trustAcFilePassword.toCharArray());
            kmf.init(ks, Constants.SERVER_KEY_STORE_PASSWORD.toCharArray());
            tmf.init(tks);
            ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            serverSocket = (SSLServerSocket) ctx.getServerSocketFactory().createServerSocket(Constants.DEFAULT_PORT);
            serverSocket.setNeedClientAuth(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
