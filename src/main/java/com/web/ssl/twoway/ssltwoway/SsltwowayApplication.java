package com.web.ssl.twoway.ssltwoway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.Socket;
import java.security.KeyStore;

@SpringBootApplication
public class SsltwowayApplication {


    public static void main(String[] args) {
        SSLServer server = new SSLServer();
        server.init();
        server.start();
        SpringApplication.run(SsltwowayApplication.class, args);
    }

}
