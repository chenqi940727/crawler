package com.yunzhitx.mediacrawler.web.rest.worldcupcrawler;

import com.yunzhitx.mediacrawler.core.media.domain.Media;
import com.yunzhitx.mediacrawler.core.mediarefcategory.domain.MediaRefCategory;
import com.yunzhitx.mediacrawler.core.poster.domain.Poster;
import com.yunzhitx.mediacrawler.core.resource.domain.Resource;
import com.yunzhitx.mediacrawler.web.rest.elasticsearch.EsController;
import com.yunzhitx.mediacrawler.web.rest.enumer.MediaPackageType;
import com.yunzhitx.mediacrawler.web.util.DownloadImage;
import tk.mybatis.mapper.util.StringUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author admin$
 * @Description: TODO
 * @date 2018/6/8$ 14:47$
 */
public class WorldCupShootTimeUtil {

    public static final String IMGURL = "http://juhe.fs.cdtown.cn/media/";
    public static final String pkgType = "资讯";
    public static final Integer cateforyId = 10000006;
    public static final Integer propertyId = 20000029;
    public static final Integer[] propertyValueIds= new Integer[]{30000513};


    public static void addWorldCupMedia(String pkgType, Integer sourceType, String title, String intro, String posterSrc, String fieldName,
                                        String playUrl){
        try {
            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
                if(mediaExsit > 0){
                    return;
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


            if(StringUtil.isNotEmpty(posterSrc)){
                //save poster
                Poster poster = new Poster();
                Boolean exsit = poster.selectPosterExsit(packageMediaId);
                if(!exsit){
                    posterSrc = DownloadImage.download(posterSrc);//下载媒资包图片
                    poster.setMediaId(packageMediaId);
                    poster.setType("title");
                    poster.setTitle(title);
                    poster.setLarge(IMGURL + posterSrc);
                    poster.setCreateDate(sdf.format(now));
                    poster.save();
                }
            }


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
        }catch (Exception e){
//            e.printStackTrace();
            System.out.println(title + "   媒资添加失败");
        }
    }
}
