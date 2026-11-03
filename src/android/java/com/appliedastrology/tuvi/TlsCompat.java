package com.appliedastrology.tuvi;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Applied Astrology v0.0.3 - Tương thích TLS cho Android 6.0 (API 23).
 * Kho CA của Android 6 thiếu các root mới (ví dụ DigiCert Global Root G2 dùng cho
 * objects/release-assets.githubusercontent.com), nên tải bản cập nhật từ GitHub Releases báo
 * "Trust anchor for certification path not found". Lớp này tạo TrustManager kiểm tra kho hệ thống TRƯỚC,
 * chỉ khi hệ thống từ chối mới thử thêm các root CA công khai đóng gói trong assets/certs (lấy từ kho Mozilla).
 * Không tắt xác minh chứng chỉ / hostname. Chỉ áp dụng cho Android < 7.0.
 */
final class TlsCompat {
    private static final String TAG = "AppliedAstrology";
    private static SSLSocketFactory factory;
    private static boolean tried;

    private TlsCompat() { }

    static void apply(Context ctx, HttpURLConnection c) {
        if (Build.VERSION.SDK_INT >= 24 || !(c instanceof HttpsURLConnection)) return;
        SSLSocketFactory f = get(ctx);
        if (f != null) ((HttpsURLConnection) c).setSSLSocketFactory(f);
    }

    private static synchronized SSLSocketFactory get(Context ctx) {
        if (tried) return factory;
        tried = true;
        try {
            TrustManagerFactory sysF = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            sysF.init((KeyStore) null);
            final X509TrustManager sys = first(sysF.getTrustManagers());
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            String[] names = ctx.getAssets().list("certs");
            int n = 0;
            if (names != null) for (String name : names) {
                if (!name.endsWith(".pem")) continue;
                InputStream in = ctx.getAssets().open("certs/" + name);
                try { ks.setCertificateEntry(name, cf.generateCertificate(in)); n++; } finally { in.close(); }
            }
            TrustManagerFactory extF = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            extF.init(ks);
            final X509TrustManager ext = first(extF.getTrustManagers());
            X509TrustManager both = new X509TrustManager() {
                @Override public void checkClientTrusted(X509Certificate[] chain, String auth) throws CertificateException { sys.checkClientTrusted(chain, auth); }
                @Override public void checkServerTrusted(X509Certificate[] chain, String auth) throws CertificateException {
                    try { sys.checkServerTrusted(chain, auth); }
                    catch (CertificateException e) { ext.checkServerTrusted(chain, auth); }   // vẫn ném lỗi nếu root đóng gói cũng không khớp
                }
                @Override public X509Certificate[] getAcceptedIssuers() {
                    X509Certificate[] a = sys.getAcceptedIssuers(), b = ext.getAcceptedIssuers();
                    X509Certificate[] r = new X509Certificate[a.length + b.length];
                    System.arraycopy(a, 0, r, 0, a.length); System.arraycopy(b, 0, r, a.length, b.length);
                    return r;
                }
            };
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{both}, null);
            factory = sc.getSocketFactory();
            Log.i(TAG, "TlsCompat: " + n + " bundled roots as fallback (API " + Build.VERSION.SDK_INT + ")");
        } catch (Exception e) {
            Log.w(TAG, "TlsCompat init " + e);
            factory = null;
        }
        return factory;
    }

    private static X509TrustManager first(TrustManager[] tms) {
        for (TrustManager t : tms) if (t instanceof X509TrustManager) return (X509TrustManager) t;
        throw new IllegalStateException("no X509TrustManager");
    }
}
