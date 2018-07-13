package com.yunzhitx.mediacrawler.web.util;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.UUID;


public class DownloadActorImg {

        private static String SAVE_PATH = "D:\\downloadActorImg";
//    private static String SAVE_PATH = "D:\\ceshi";

    public static String download(String urlString) throws Exception {
        String filename = UUID.randomUUID().toString().substring(0, 8) + new Date().getTime() + (int) (Math.random() * 1000) + ".jpg";
//        URL url = new URL(null,urlString,new sun.net.www.protocol.https.Handler());//重点在这里，需要使用带有URLStreamHandler参数的URL构造方法
//        javax.net.ssl.HttpsURLConnection httpConnection = (javax.net.ssl.HttpsURLConnection) url.openConnection();//由于我调用的是官方给微信API接口，所以采用HTTPS连接

        URL url = new URL(urlString);
        URLConnection con = url.openConnection();
        con.setConnectTimeout(1000);
        InputStream is = null;
        try {
            is = con.getInputStream();
        } catch (Exception e) {
            return null;
        }
        byte[] bs = new byte[1024];
        int len;
        File sf = new File(SAVE_PATH);
        if (!sf.exists()) {
            sf.mkdirs();
        }
        OutputStream os = new FileOutputStream(sf.getPath() + "\\" + filename);
        while ((len = is.read(bs)) != -1) {
            os.write(bs, 0, len);
        }
        os.close();
        is.close();
        return filename;
    }

    public static void main(String[] args) {
        try {
            String ss = download("http://i.gtimg.cn/qqlive/img/jpgcache/files/qqvideo/0/0rughzovkp24fc3.jpg");
//			download("英雄使命","ss");
//			download("健康有道","ss");
//			download("森巴幸福岛","ss");
            System.out.println(ss);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}  