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
import org.springframework.util.StringUtils;


/**
 * @author admin$
 * @Description: TODO
 * @date 2018/5/30$ 15:43$
 */
public class TxTVCrawler extends BreadthCrawler{

    private static volatile Integer count = 0;

    private Integer sourceType = SourceType.TYPE_TENXUN.getIndex(); //爱奇艺视频id
    private static final String fieldName = SourceType.TYPE_TENXUN.getName();
    private static final String pkgType = MediaType.TYPE_TV.getName();
    private static final String IMGURL = "http://juhe.fs.cdtown.cn/media/";
    private static final String tenantFlag = "yztx";

    /**
     * 构造一个基于伯克利DB的爬虫
     * 伯克利DB文件夹为crawlPath，crawlPath中维护了历史URL等信息
     * 不同任务不要使用相同的crawlPath
     * 两个使用相同crawlPath的爬虫并行爬取会产生错误
     *  @param crawlPath 伯克利DB使用的文件夹
     * @param autoParse 是否根据设置的正则自动探测新URL
     * @param redisDao
     */
    public TxTVCrawler(String crawlPath, boolean autoParse, BaseRedisDao redisDao) {
        super(crawlPath, autoParse);
        this.redisDao = redisDao;
    }

    private BaseRedisDao redisDao;

    @Override
    public void visit(Page page, CrawlDatums next) {
        Element ul = page.select("ul.figures_list").get(0);
        Elements tvList = ul.select("li");
        for(Element tv : tvList){
            Element a = tv.select("a.figure").first();
            String txTvId = a.attr("data-float");
            Element img = a.select("img").first();
            String name = img.attr("alt");

            redisDao.addList(RedisKey.TV_TXID_LIST, txTvId);
            redisDao.addMap(RedisKey.TV_TXID_NAME, txTvId, name + " - " + page.url());
        }
    }




}
