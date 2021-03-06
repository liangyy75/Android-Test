package com.liang.example.volleytest;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.liang.example.utils.ApiManager;

public class HTTPSTrustManager implements X509TrustManager {
    private static final String TAG = "HTTPSTrustManager";
    private static TrustManager[] trustManagers;
    private static final X509Certificate[] _AcceptedIssuers = new X509Certificate[]{};

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return _AcceptedIssuers;
    }

    public static void allowAllSSL() {
        ApiManager.LOGGER.d(TAG, "allowAllSSL");
        HttpsURLConnection.setDefaultHostnameVerifier((arg0, arg1) -> {
            // TODO Auto-generated method stub
            return true;
        });
        SSLContext context = null;
        if (trustManagers == null) {
            trustManagers = new TrustManager[]{new HTTPSTrustManager()};
        }
        try {
            context = SSLContext.getInstance("TLS");
            context.init(null, trustManagers, new SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            ApiManager.LOGGER.e(TAG, "NoSuchAlgorithmException", e);
        } catch (KeyManagementException e) {
            ApiManager.LOGGER.e(TAG, "KeyManagementException", e);
        }
        assert context != null;
        HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
    }
}
