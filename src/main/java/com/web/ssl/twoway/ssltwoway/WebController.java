package com.web.ssl.twoway.ssltwoway;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.cert.X509Certificate;

@RequestMapping(value = "**")
@Controller
public class WebController {
    @RequestMapping(value = "**")
    @ResponseBody
    public Object test(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
        boolean certvalid = false;
        if (certs != null && certs.length > 0) {
            int count = certs.length;
            System.out.println("total certs num:" + count);
            for (int i = 0; i < count; i++) {
                System.out.println("client cert:" + (++i));
                if (verifyCertificate(certs[--i])) {
                    certvalid = true;
                    System.out.println("证书验证结果SUCCESS");
                }

                System.out.println("client cert:" + certs[i].toString());
            }
        } else {
            if ("https".equals(request.getScheme())) {
                System.out.println("https yes but no cert:");
            } else {
                System.out.println("http yes reject:");
            }
        }
        return "ResponseBody:SUCCES" + certvalid;
    }

    private boolean verifyCertificate(X509Certificate cert) {
        return false;
    }
}
