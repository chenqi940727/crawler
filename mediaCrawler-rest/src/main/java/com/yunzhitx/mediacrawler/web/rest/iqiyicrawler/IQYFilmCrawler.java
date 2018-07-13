package com.yunzhitx.mediacrawler.web.rest.iqiyicrawler;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;
import com.yunzhitx.mediacrawler.core.media.domain.Media;
import com.yunzhitx.mediacrawler.web.rest.enumer.MediaPackageType;
import com.yunzhitx.mediacrawler.web.rest.enumer.MediaType;
import com.yunzhitx.mediacrawler.web.rest.enumer.SourceType;
import com.yunzhitx.mediacrawler.web.util.CqUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author admin$
 * @Description: TODO
 * @date 2018/5/30$ 15:43$
 */
public class IQYFilmCrawler extends BreadthCrawler{

    private static volatile Integer count = 0;

    private Integer sourceType = SourceType.TYPE_IQIYI.getIndex(); //爱奇艺视频id
    private static final String fieldName = SourceType.TYPE_IQIYI.getName();
    private static final String pkgType = MediaType.TYPE_FILM.getName();
    private static final String IMGURL = "http://juhe.fs.cdtown.cn/media/";
    private static final String tenantFlag = "yztx";

    /**
     * 构造一个基于伯克利DB的爬虫
     * 伯克利DB文件夹为crawlPath，crawlPath中维护了历史URL等信息
     * 不同任务不要使用相同的crawlPath
     * 两个使用相同crawlPath的爬虫并行爬取会产生错误
     *
     * @param crawlPath 伯克利DB使用的文件夹
     * @param autoParse 是否根据设置的正则自动探测新URL
     */
    public IQYFilmCrawler(String crawlPath, boolean autoParse) {
        super(crawlPath, autoParse);
    }

    @Override
    public void visit(Page page, CrawlDatums next) {
        if(page.matchUrl("http://www.iqiyi.com/v_*.html#vfrm=2-4-0-1")){
            String filmName = page.select("span[id=playerAreaScore]").text().trim();
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("fd_name",filmName);
            params.put("fd_pkgType",pkgType);
            params.put("fd_type", MediaPackageType.TYPE_MEDIA.getIndex());
            Integer mediaExsit = Media.getMediaRepository().countByMap(params);
            if(mediaExsit == 1){
                return;
            }else{
                params.put("fd_type", MediaPackageType.TYPE_MEDIA.getIndex());
                Integer mediaPackageExsit = Media.getMediaRepository().countByMap(params);
                //获取媒资信息，media,poster,performer,resource,property...
                //media

                String snsscore = page.select("span[id=playerAreaScore]").attr("snsscore");
                Integer doubanScore = CqUtils.getPercent(snsscore);
                if(mediaPackageExsit == 0){
                    //TODO
                    // 添加媒资包
                }
                //TODO
                //添加子集
            }
        }
    }




}
