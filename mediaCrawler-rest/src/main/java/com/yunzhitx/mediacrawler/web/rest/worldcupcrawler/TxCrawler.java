package com.yunzhitx.mediacrawler.web.rest.worldcupcrawler;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;
import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.druid.util.StringUtils;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author admin$
 * @Description: TODO
 * @date 2018/6/8$ 10:53$
 */
public class TxCrawler extends BreadthCrawler {
    /**
     * 构造一个基于伯克利DB的爬虫
     * 伯克利DB文件夹为crawlPath，crawlPath中维护了历史URL等信息
     * 不同任务不要使用相同的crawlPath
     * 两个使用相同crawlPath的爬虫并行爬取会产生错误
     *
     * @param crawlPath 伯克利DB使用的文件夹
     * @param autoParse 是否根据设置的正则自动探测新URL
     */
    public TxCrawler(String crawlPath, boolean autoParse) {
        super(crawlPath, autoParse);
    }

    public static final String IMGURL = "http://juhe.fs.cdtown.cn/media/";
    public static final int sourceType = SourceType.TYPE_TENXUN.getIndex();
    public static final String fieldName = SourceType.TYPE_TENXUN.getName();
    public static final String pkgType = "资讯";
    public static final Integer cateforyId = 10000006;
    public static final Integer propertyId = 20000029;
    public static final Integer[] propertyValueIds= new Integer[]{30000443, 30000485, 30000487};
    public static final String splitTag = "-yztx-";


    @Override
    public void visit(Page page, CrawlDatums next) {
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dataInfo = page.select("div[id=articleLiInHidden] script").text();
        dataInfo = dataInfo.replace("//列表数据", "");
        dataInfo = dataInfo.split("ARTICLE_LIST=")[1];
        if(StringUtils.isEmpty(dataInfo)){
            return;
        }
        Map infoMap = (Map) JSONUtils.parse(dataInfo);
        List<Map> list = (List<Map>) infoMap.get("listInfo");
        for(Map info : list){
            String title = (String) info.get("title");
            String playUrl = (String) info.get("url");
            String imgSrc = (String) info.get("imgurl");

            WorldCupUtil.addWorldCupMedia(pkgType, sourceType, title, title, imgSrc, fieldName, playUrl);
        }

    }
}
