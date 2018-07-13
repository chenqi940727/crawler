package com.yunzhitx.mediacrawler.web.rest.timer;

import com.yunzhitx.mediacrawler.web.rest.worldcupcrawler.BallKingCrawler;
import com.yunzhitx.mediacrawler.web.rest.worldcupcrawler.CCTVWorldCupCrawler;
import com.yunzhitx.mediacrawler.web.rest.worldcupcrawler.TxCrawler2;
import com.yunzhitx.mediacrawler.web.rest.worldcupcrawler.YoukuLookBackCrawler;
import com.yunzhitx.mediacrawler.web.util.SFTPUtil;
import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;

/**
 * @author admin$
 * @Description: TODO
 * @date 2018/6/12$ 11:57$
 */
@Component
public class Timer {

    Logger logger = Logger.getLogger(Timer.class);


    private RestTemplate restTemplate = new RestTemplate();


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

}
