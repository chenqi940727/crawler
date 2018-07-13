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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author admin$
 * @Description: TODO
 * @date 2018/6/8$ 10:17$
 */
public class QiutanCrawler extends BreadthCrawler {
    /**
     * 构造一个基于伯克利DB的爬虫
     * 伯克利DB文件夹为crawlPath，crawlPath中维护了历史URL等信息
     * 不同任务不要使用相同的crawlPath
     * 两个使用相同crawlPath的爬虫并行爬取会产生错误
     *
     * @param crawlPath 伯克利DB使用的文件夹
     * @param autoParse 是否根据设置的正则自动探测新URL
     */
    public QiutanCrawler(String crawlPath, boolean autoParse) {
        super(crawlPath, autoParse);
    }

    public static final String IMGURL = "http://juhe.fs.cdtown.cn/media/";
    public static final int sourceType = SourceType.TYPE_QIUTAN.getIndex();
    public static final String fieldName = SourceType.TYPE_QIUTAN.getName();
    public static final String pkgType = "资讯";
    public static final Integer cateforyId = 10000006;
    public static final Integer propertyId = 20000029;
    public static final Integer[] propertyValueIds= new Integer[]{30000443, 30000485, 30000487};
    public static final String splitTag = "-yztx-";


    @Override
    public void visit(Page page, CrawlDatums next) {
        try {
            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Elements newsList = page.select("#news_list ol li");
            if(newsList.size() == 0){
                return;
            }
            for(Element news : newsList){
                Element aTag = news.select("a").get(0);
                String playUrl = aTag.attr("href");
                String imgSrc = aTag.select("img").attr("src");
                String title = news.select("h2 > a").text();
                String intro = news.select("p").text();

                Media media = new Media();
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("fd_name",title);
                params.put("fd_pkgType",pkgType);
                params.put("fd_type", MediaPackageType.TYPE_SINGLE.getIndex());
                Integer packageMediaId = media.selectMediaIdIfMediaPackageExsit(params);
                if(packageMediaId != null){
                    params.clear();
                    params.put("fd_resourceTypeId", sourceType);
                    params.put("packageMediaId", packageMediaId);
                    Integer mediaExsit = media.selectMediaResoureExsit(params);
                    if(mediaExsit == 1){
                        continue;
                    }
                }


                media.setName(title);
                media.setPkgType(pkgType);
                media.setTerminal("pc+app");
                media.setState(1 + "");
                media.setTotal(1);
                media.setSerial(1);
                media.setIntroduce(intro);
                media.setDetail(intro);
                media.setCreateDate(sdf.format(now));
                media.setUpdateDate(sdf.format(now));
                media.setPlayCount(0L);
                if(packageMediaId == null){
                    media.setParentId(0);
                    media.setType(MediaPackageType.TYPE_SINGLE.getIndex());
                    media.save();
                    packageMediaId = media.getId();
                }
                media.setId(null);
                media.setType(MediaPackageType.TYPE_MEDIA.getIndex());
                media.setParentId(packageMediaId);
                media.save();

                //save poster
                Poster poster = new Poster();
                imgSrc = DownloadImage.download(imgSrc);//下载媒资包图片

                poster.setMediaId(packageMediaId);
                poster.setType("title");
                poster.setTitle(title);
                poster.setLarge(IMGURL + imgSrc);
                poster.setCreateDate(sdf.format(now));
                poster.save();

                Resource resource = new Resource();
                resource.setMediaId(media.getId());
                resource.setResourceTypeId(sourceType);
                resource.setFieldName(fieldName);
                resource.setType("main");
                resource.setPlayType("third");
                resource.setName(title);
                resource.setPlayUrl(playUrl);
                resource.setState(1 + "");
                resource.setKeyword(title);
                resource.setTerminal("pc+app");
                resource.setPlayCount(0);
                resource.setCreateDate(sdf.format(now));
                resource.save();

                for(Integer propertyValueId : propertyValueIds){
                    MediaRefCategory mediaRefCategory = new MediaRefCategory();
                    mediaRefCategory.setMediaId(packageMediaId);
                    mediaRefCategory.setCategoryId(cateforyId);
                    mediaRefCategory.setPropertyId(propertyId);
                    mediaRefCategory.setProperty("类型");
                    mediaRefCategory.setPropertyValueId(propertyValueId);
                    mediaRefCategory.save();
                }
                Media media2 = Media.getMediaRepository().getMediaDetail(packageMediaId);
                EsController.addEsDate(media2);
            }
        } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("出错了哦,好气啊");
        }
    }
}
