package com.yunzhitx.mediacrawler.web.rest.txcrawler;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;
import com.yunzhitx.mediacrawler.web.rest.enumer.MediaType;
import com.yunzhitx.mediacrawler.web.rest.enumer.SourceType;
import com.yunzhitx.mediacrawler.web.rest.redis.BaseRedisDao;
import com.yunzhitx.mediacrawler.web.util.RedisKey;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author admin$
 * @Description: TODO
 * @date 2018/5/30$ 15:43$
 */
public class TxVarietyCrawler extends BreadthCrawler{

    private static volatile Integer count = 0;

    private Integer sourceType = SourceType.TYPE_TENXUN.getIndex(); //腾讯视频id
    private static final String fieldName = SourceType.TYPE_TENXUN.getName();
    private static final String varietypkgType = MediaType.TYPE_VARIETY.getName();
    private static final String IMGURL = "http://juhe.fs.cdtown.cn/media/";
    private static final String tenantFlag = "yztx";
    private Pattern itemPattern = Pattern.compile("(?<=detail/.{1}/)(.+?)(?=.html)");
    /**
     * 构造一个基于伯克利DB的爬虫
     * 伯克利DB文件夹为crawlPath，crawlPath中维护了历史URL等信息
     * 不同任务不要使用相同的crawlPath
     * 两个使用相同crawlPath的爬虫并行爬取会产生错误
     *  @param crawlPath 伯克利DB使用的文件夹
     * @param autoParse 是否根据设置的正则自动探测新URL
     * @param redisDao
     */
    public TxVarietyCrawler(String crawlPath, boolean autoParse, BaseRedisDao redisDao) {
        super(crawlPath, autoParse);
        this.redisDao = redisDao;
    }

    private BaseRedisDao redisDao;

    @Override
    public void visit(Page page, CrawlDatums next) {
        if (page.matchUrl("https://v.qq.com/x/cover/[0-9A-Za-z]{15}.html")) {
            Element parent = null;
            try {
                parent = page.select("div.player_header h2.player_title a").first();
            }catch (Exception e){
                e.printStackTrace();
                return;
            }
            String txVarietyId = parent.attr("href");
            String name = parent.text();
            txVarietyId = getVarietyIdByUrl(txVarietyId, itemPattern);
            redisDao.addList(RedisKey.TX_VARIETY_ALBUMID_LIST, txVarietyId);
            redisDao.addMap(RedisKey.ID_TO_NAME, txVarietyId, name + " - " + page.url());
        }

//        Element ul = page.select("ul.figures_list").get(0);
//        Elements tvList = ul.select("li");
//        for(Element tv : tvList){
//            Element a = tv.select("a.figure").first();
//            String txTvId = a.attr("data-float");
//            Element img = a.select("img").first();
//            String name = img.attr("alt");
//
//            redisDao.addList(RedisKey.TX_VARIETY_ALBUMID_LIST, txTvId);
//            redisDao.addMap(RedisKey.ID_TO_NAME, txTvId, name + " - " + page.url());
//        }
    }

    private static String getVarietyIdByUrl(String url, Pattern pattern) {
        String targetString = null;
        Matcher matcher = pattern.matcher(url);
        while(matcher.find()){
            targetString = matcher.group();
        }
        return targetString;
    }

//    public static void main (String[] args){
//        Pattern itemPattern = Pattern.compile("(?<=detail/.{1}/)(.+?)(?=.html)");
//        String ss = getVarietyIdByUrl("/detail/7/77164.html",itemPattern);
//        System.out.println(ss);
//    }

}
