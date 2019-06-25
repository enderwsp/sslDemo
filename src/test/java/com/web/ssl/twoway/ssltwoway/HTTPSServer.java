package com.web.ssl.twoway.ssltwoway;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.KeyStore;
import java.security.cert.Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.security.cert.X509Certificate;

public class HTTPSServer {
    private boolean isServerDone = false;

    public static void main(String[] args) {
        HTTPSServer server = new HTTPSServer();
        server.run();
    }

    // Create the and initialize the SSLContext
    private SSLContext createSSLContext() {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(new FileInputStream(Constants.serverPrivateFile), Constants.serverPrivatePassword.toCharArray());

            // Create key manager
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, Constants.serverPrivatePassword.toCharArray());
            KeyManager[] km = keyManagerFactory.getKeyManagers();
            KeyStore keyStoreTrust = KeyStore.getInstance("JKS");
            keyStoreTrust.load(new FileInputStream(Constants.trustAcFile), Constants.trustAcFilePassword.toCharArray());
            // Create trust manager
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keyStoreTrust);
            TrustManager[] tm = trustManagerFactory.getTrustManagers();

            // Initialize SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(km, tm, null);

            return sslContext;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    // Start to run the server
    public void run() {
        SSLContext sslContext = this.createSSLContext();

        try {
            // Create server socket factory
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();

            // Create server socket
            SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(Constants.DEFAULT_PORT);

            System.out.println("SSL server started");
            while (!isServerDone) {
                SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();

                // Start the server thread
                new ServerThread(sslSocket).start();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Thread handling the socket from client
    static class ServerThread extends Thread {
        private SSLSocket sslSocket = null;

        ServerThread(SSLSocket sslSocketInit) {
            sslSocket = sslSocketInit;
            sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());
            sslSocket.setEnabledProtocols(sslSocket.getSupportedProtocols());
            sslSocket.setNeedClientAuth(true);
            sslSocket.setWantClientAuth(true);
            sslSocket.setEnableSessionCreation(true);
        }

        public void run() {

            try {
                // Start handshake
                sslSocket.startHandshake();

                // Get session after the connection is established
                SSLSession sslSession = sslSocket.getSession();

                System.out.println("SSLSession :");
                System.out.println("\tProtocol : " + sslSession.getProtocol());
                System.out.println("\tCipher suite : " + sslSession.getCipherSuite());

                // Start handling application content
                InputStream inputStream = sslSocket.getInputStream();
                OutputStream outputStream = sslSocket.getOutputStream();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream));

                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    System.out.println("Inut : " + line);

                    if (line.trim().isEmpty()) {
                        break;
                    }
                }
                boolean clientCertValid = false;
                try {
                    Certificate[] c1 = sslSession.getLocalCertificates();
                    for (Certificate c1One : c1) {
                        System.out.println("getLocalCertificates : " + c1One.toString());
                    }
                    X509Certificate[] c2 = sslSession.getPeerCertificateChain();
                    for (X509Certificate c1One : c2) {
                        System.out.println("getPeerCertificateChain : " + c1One.toString());
                    }
                    Certificate[] c3 = sslSession.getPeerCertificates();
                    for (Certificate c1One : c3) {
                        System.out.println("getPeerCertificates : " + c1One.toString());
                    }
                    clientCertValid = true;
                } catch (Exception cex) {

                }
                if (clientCertValid) {
                    // Write data
                    printWriter.print("HTTP/1.1 200 OK\r\nServer: johnserver/1.0.8.18\r\nContent-Length: 3\r\n\r\nok\n");

                    printWriter.flush();
                } else {
                    // Write data
                    printWriter.print("HTTP/1.1 200 FAILED\r\nServer: johnserver/1.0.8.18\r\nContent-Length: 6\r\n\r\nFAILED\n");

                    printWriter.flush();
                }


                sslSocket.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
