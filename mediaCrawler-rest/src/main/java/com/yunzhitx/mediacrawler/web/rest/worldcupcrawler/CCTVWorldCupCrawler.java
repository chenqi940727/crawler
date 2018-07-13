package com.yunzhitx.mediacrawler.web.rest.worldcupcrawler;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;
import com.yunzhitx.mediacrawler.core.media.domain.Media;
import com.yunzhitx.mediacrawler.core.mediarefcategory.domain.MediaRefCategory;
import com.yunzhitx.mediacrawler.core.poster.domain.Poster;
import com.yunzhitx.mediacrawler.core.resource.domain.Resource;
import com.yunzhitx.mediacrawler.web.rest.elasticsearch.EsController;
import com.yunzhitx.mediacrawler.web.rest.enumer.MediaPackageType;
import com.yunzhitx.mediacrawler.web.rest.enumer.SourceType;
import com.yunzhitx.mediacrawler.web.util.DownloadImage;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @author admin$
 * @Description: TODO
 * @date 2018/6/5$ 10:05$
 */
public class CCTVWorldCupCrawler extends BreadthCrawler {


    public static final String IMGURL = "http://juhe.fs.cdtown.cn/media/";
    public static final int sourceType = SourceType.TYPE_CCTV.getIndex();
    public static final String fieldName = "CCTV";
    public static final String pkgType = "资讯";
    public static final Integer cateforyId = 10000006;
    public static final Integer propertyId = 20000029;
    public static final Integer[] propertyValueIds= new Integer[]{30000443, 30000485, 30000487};
    public static final String splitTag = "-yztx-";

//花编世界杯 世界杯焦点 世界杯独家
    /**
     * 构造一个基于伯克利DB的爬虫
     * 伯克利DB文件夹为crawlPath，crawlPath中维护了历史URL等信息
     * 不同任务不要使用相同的crawlPath
     * 两个使用相同crawlPath的爬虫并行爬取会产生错误
     *
     * @param crawlPath 伯克利DB使用的文件夹
     * @param autoParse 是否根据设置的正则自动探测新URL
     */
    public CCTVWorldCupCrawler(String crawlPath, boolean autoParse) {
        super(crawlPath, autoParse);
    }

    @Override
    public void visit(Page page, CrawlDatums next) {
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Element content = page.select("div.imagelist_lt5 > .image_list_box").get(0);
        Elements videoList = content.select("li");
        System.out.println(videoList.size());
        for(Element video : videoList){
            Element aTag = video.select("div.image > a").get(0);
            String playUrl = aTag.attr("href");
            String imgSrc = aTag.select("img").attr("src");
            String desc = video.select("div.text > a").text();

            WorldCupUtil.addWorldCupMedia(pkgType, sourceType, desc, desc, imgSrc, fieldName, playUrl);
    }
    }
}
