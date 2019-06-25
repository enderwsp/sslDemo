package com.web.ssl.twoway.ssltwoway;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Constants {
    final static String clientPrivatePassword = "123qwe";
    final static String serverPrivatePassword = "123123";
    final static String clientPrivateFile = "client.p12";
    final static String serverPrivateFile = "localhost.p12";
    final static String trustAcFile = "trustme.jks";
    final static String trustAcFilePassword = "1q2w3e";
    final static String SERVER_KEY_STORE_PASSWORD = "1qa2ws";
    final static String CLIENT_KEY_STORE_PASSWORD = "12qwas";
    final static int DEFAULT_PORT = 8443;
    public static ThreadPoolExecutor httpExecutor = new ThreadPoolExecutor(20, 100, 60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(800));
}
