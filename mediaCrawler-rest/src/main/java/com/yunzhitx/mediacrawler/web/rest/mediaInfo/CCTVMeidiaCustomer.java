package com.yunzhitx.mediacrawler.web.rest.mediaInfo;

import com.alibaba.fastjson.JSON;
import com.yunzhitx.mediacrawler.web.rest.IQYService.IQYUtils;
import com.yunzhitx.mediacrawler.web.rest.redis.BaseRedisDao;
import com.yunzhitx.mediacrawler.web.rest.redis.BeanContext;
import com.yunzhitx.mediacrawler.web.util.RedisKey;
import com.yunzhitx.mediacrawler.web.util.SFTPUtil;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author admin$
 * @Description: TODO
 * @date 2018/5/31$ 17:53$
 */
public class CCTVMeidiaCustomer implements Runnable {

    private BaseRedisDao redisDao;

    private static RestTemplate restTemplate = new RestTemplate();


    @Override
    public void run() {
        this.redisDao = BeanContext.getApplicationContext().getBean(BaseRedisDao.class);
        Boolean flag = true;
        while(flag){
            SFTPUtil sftpUtil = SFTPUtil.getInstance();
            sftpUtil.login();
            if(redisDao.exists(RedisKey.CCTV_VIDEO_WC) && redisDao.getListSize(RedisKey.CCTV_VIDEO_WC) > 0){
                Long startTime = System.currentTimeMillis();
                String info = redisDao.lPop(RedisKey.CCTV_VIDEO_WC).toString();
                String playUrl = info.split("-yztx-")[0];
                Connection connect = Jsoup.connect(playUrl.toString());
                try {
                    Document document = connect.ignoreContentType(true).data("query", "Java").userAgent("Mozilla").cookie("auth", "token").timeout(3000).get();
                    System.out.println("zzzzz");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                flag = false;
                sftpUtil.logout();
            }
        }

    }
}
