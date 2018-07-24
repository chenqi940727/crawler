package com.yunzhitx.mediacrawler.web.rest.timer;

import com.yunzhitx.mediacrawler.web.rest.iqiyicrawler.IQYAnimeCrawler;
import com.yunzhitx.mediacrawler.web.rest.iqiyicrawler.IQYFilmTestCrawler;
import com.yunzhitx.mediacrawler.web.rest.iqiyicrawler.IQYTVCrawler;
import com.yunzhitx.mediacrawler.web.rest.mediaInfo.*;
import com.yunzhitx.mediacrawler.web.rest.redis.BaseRedisDao;
import com.yunzhitx.mediacrawler.web.rest.txcrawler.TxAnimeCrawler;
import com.yunzhitx.mediacrawler.web.rest.txcrawler.TxFilmCrawler;
import com.yunzhitx.mediacrawler.web.rest.txcrawler.TxTVCrawler;
import com.yunzhitx.mediacrawler.web.rest.txcrawler.TxVarietyCrawler;
import com.yunzhitx.mediacrawler.web.rest.worldcupcrawler.CCTVWorldCupCrawler;
import com.yunzhitx.mediacrawler.web.rest.worldcupcrawler.TxCrawler2;
import com.yunzhitx.mediacrawler.web.rest.worldcupcrawler.YoukuLookBackCrawler;
import com.yunzhitx.mediacrawler.web.util.SFTPUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @author admin$
 * @Description: TODO
 * @date 2018/6/12$ 11:57$
 */
@Component
public class Timer {

    Logger logger = Logger.getLogger(Timer.class);


    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private BaseRedisDao<String, Object> redisDao;


//    @Scheduled(cron = "0 0/5 * * * ?")
    public void crawlerTimer() {
        logger.info("timer start");
        System.out.println("timer start");
        Long startTime = System.currentTimeMillis();
        SFTPUtil sftpUtil = SFTPUtil.getInstance();
        sftpUtil.login();
        try {
            CCTVWorldCupCrawler crawler2 = new CCTVWorldCupCrawler("cctvCrawler", false);
            crawler2.addSeed("http://worldcup.cctv.com/2018/videos/index.shtml");
            crawler2.getConf().setTopN(300);
            crawler2.setMaxExecuteCount(3);
            crawler2.setThreads(1);
            crawler2.start(1);

//            BallKingCrawler.goGetBallKingWorldCupMedia(10,restTemplate);

            TxCrawler2.visit(10,restTemplate);

            Long endTime = System.currentTimeMillis();
            logger.info("timer end, hs: " + (endTime - startTime));
            System.out.println("timer end, hs: " + (endTime - startTime));
        }catch (Exception e){
            System.out.println("error: zzzzzzzzz");
        }finally {
//            sftpUtil.logout();
        }
    }

//    @Scheduled(cron = "0 0/30 * * * ?")
    public void lookBackMediaTimer() {
        logger.info("lookBackMediaTimer start");
        System.out.println("lookBackMediaTimer start");
        Long startTime = System.currentTimeMillis();
        SFTPUtil sftpUtil = SFTPUtil.getInstance();
        sftpUtil.login();
        try {
            YoukuLookBackCrawler.visit();
            Long endTime = System.currentTimeMillis();
            logger.info("lookBackMediaTimer end, hs: " + (endTime - startTime));
            System.out.println("lookBackMediaTimer end, hs: " + (endTime - startTime));
        }catch (Exception e){
            System.out.println("error: zzzzzzzzz");
        }finally {
//            sftpUtil.logout();
        }
    }

    @Scheduled(cron = "0 30 10,14,18,22,0 * * ?")
    public void mediaCrawler() {
        logger.info("mediaCrawler start");
        Long startTime = System.currentTimeMillis();
        SFTPUtil sftpUtil = SFTPUtil.getInstance();
        sftpUtil.login();
        try {
            iqyFilmCrawl();
            iqyTvCrawl();
            iqyAnimeCrawl();
            txFilmCrawl();
            txTvCrawl();
            txAnimeCrawl();
            txVarietyCrawl();
            Long endTime = System.currentTimeMillis();
            logger.info("mediaCrawler end, hs: " + (endTime - startTime));
        }catch (Exception e){
            System.out.println("error: zzzzzzzzz");
        }finally {
            sftpUtil.logout();
        }
    }

    private void iqyAnimeCrawl() throws Exception {
        Integer length = 50;
        IQYAnimeCrawler crawler = new IQYAnimeCrawler("iqyAnimeCrawler", true,redisDao);
        for (int i = 1; i <= length; i++) {
            crawler.addSeed("http://list.iqiyi.com/www/4/-------------4-"+ i +"-1-iqiyi--.html");
            crawler.addSeed("http://list.iqiyi.com/www/4/-------------11-"+ i +"-1-iqiyi--.html");
        }
        crawler.getConf().setTopN(300);
        crawler.setMaxExecuteCount(3);
        crawler.start(1);
//        for(int i=0; i < 10; i++){
        IqyAnimeCustomer iqyAnimeCustomer = new IqyAnimeCustomer();
        Thread iqyAnimeCustomerThred = new Thread(iqyAnimeCustomer);
        iqyAnimeCustomerThred.start();
    }

    private void txVarietyCrawl() throws Exception {
        Integer length = 30;
        TxVarietyCrawler crawler = new TxVarietyCrawler("txVarietyCrawler", true,redisDao);
        for (int i = 0; i <= length; i++) {
            crawler.addSeed("http://v.qq.com/x/list/variety?sort=4&offset=" + i * 30);
            crawler.addSeed("http://v.qq.com/x/list/variety?sort=5&offset=" + i * 30);
        }
        crawler.addRegex("https://v.qq.com/x/cover/[0-9A-Za-z]{15}.html");
        crawler.getConf().setTopN(300);
        crawler.setMaxExecuteCount(3);
        crawler.start(2);
        TxVarietyCustomer txVarietyCustomer = new TxVarietyCustomer();
        Thread txVarietyCustomerThred = new Thread(txVarietyCustomer);
        txVarietyCustomerThred.start();
    }

    private void txAnimeCrawl() throws Exception {
        Integer length = 50;
        TxAnimeCrawler crawler = new TxAnimeCrawler("txAnimeCrawler", true,redisDao);
        for (int i = 0; i <= length; i++) {
            crawler.addSeed("http://v.qq.com/x/list/cartoon?sort=18&offset=" + i * 30);
            crawler.addSeed("http://v.qq.com/x/list/cartoon?sort=19&offset=" + i * 30);
        }
        crawler.getConf().setTopN(300);
        crawler.setMaxExecuteCount(3);
        crawler.start(1);
        Thread.sleep(1000);
        TxAnimeCustomer txAnimeCustomer = new TxAnimeCustomer();
        Thread txAnimeCustomerThred = new Thread(txAnimeCustomer);
        txAnimeCustomerThred.start();
    }

    private void txFilmCrawl() throws Exception {
        Integer length = 20;
        TxFilmCrawler crawler = new TxFilmCrawler("txFilmCrawler", true,redisDao);
        for (int i = 0; i <= length; i++) {
            crawler.addSeed("http://v.qq.com/x/list/movie?sort=19&offset=" + i * 30);
            crawler.addSeed("http://v.qq.com/x/list/movie?sort=18&offset=" + i * 30);
        }
        crawler.getConf().setTopN(300);
        crawler.setMaxExecuteCount(3);
        crawler.start(1);
        Thread.sleep(1000);
        TxFilmCustomer txFilmCustomer = new TxFilmCustomer();
        Thread txFilmCustomerThread = new Thread(txFilmCustomer);
        txFilmCustomerThread.start();
    }

    private void iqyFilmCrawl() throws Exception {
        Integer length = 20;
        IQYFilmTestCrawler crawler2 = new IQYFilmTestCrawler("iqyFilmCrawler", true,redisDao);
        for (int i = 1; i <= length; i++) {
            crawler2.addSeed("http://list.iqiyi.com/www/1/-------------4-" + i + "-1-iqiyi--.html");
            crawler2.addSeed("http://list.iqiyi.com/www/1/-------------11-" + i + "-1-iqiyi--.html");
        }
        crawler2.getConf().setTopN(300);
        crawler2.setMaxExecuteCount(3);
        crawler2.start(1);
        Thread.sleep(1000);
//        for(int i=0; i < 10; i++){
        IqyFilmCustomer meidiaCustomer = new IqyFilmCustomer();
        Thread mediaCustomerThread = new Thread(meidiaCustomer);
        mediaCustomerThread.start();
    }

    private void txTvCrawl() throws Exception {
        Integer length = 50;
        TxTVCrawler crawler = new TxTVCrawler("txTvCrawler", true,redisDao);
        for (int i = 0; i <= length; i++) {
            crawler.addSeed("http://v.qq.com/x/list/tv?sort=19&offset=" + i * 30);
            crawler.addSeed("http://v.qq.com/x/list/tv?sort=18&offset=" + i * 30);
        }
        crawler.getConf().setTopN(300);
        crawler.setMaxExecuteCount(3);
        crawler.start(1);
        Thread.sleep(1000);
        TxTvCustomer txTvCustomer = new TxTvCustomer();
        Thread txTvCustomerThred = new Thread(txTvCustomer);
        txTvCustomerThred.start();
    }

    private void iqyTvCrawl() throws Exception {
        Integer length = 50;
        IQYTVCrawler crawler2 = new IQYTVCrawler("iqyTvCrawler", true,redisDao);
        for (int i = 1; i <= length; i++) {
            crawler2.addSeed("http://list.iqiyi.com/www/2/-------------11-" + i + "-1---.html");
            crawler2.addSeed("http://list.iqiyi.com/www/2/-------------4-" + i + "-1---.html");
        }
        crawler2.getConf().setTopN(300);
        crawler2.setMaxExecuteCount(3);
        crawler2.start(1);
        Thread.sleep(1000);
        IqyTVCrawlerCustomer iqytvCrawlerCustomer = new IqyTVCrawlerCustomer();
        Thread iqytvCrawlerCustomerThread = new Thread(iqytvCrawlerCustomer);
        iqytvCrawlerCustomerThread.start();
    }

}
