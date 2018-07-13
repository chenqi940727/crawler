package com.yunzhitx.mediacrawler.core.haokan.domain;

import com.yunzhitx.cloud.common.model.ddd.InstanceFactory;
import com.yunzhitx.mediacrawler.core.haokan.infra.AssetPosterRepository;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * @author zj
 * @Description: 海报
 */
@Data
@Table(name = "asset_poster")
public class AssetPoster {
    @Column(name = "id")
    private String id;                //海报ID
    @Column(name = "resourceCode")
    private String resourceCode;    //媒资ID（对应Asset）
    @Column(name = "fileName")
    private String fileName;        //文件名称
    @Column(name = "fileSize")
    private int fileSize;        //文件大小
    @Column(name = "height")
    private int height;            //高度
    @Column(name = "width")
    private int width;            //宽度
    @Column(name = "localPath")
    private String localPath;        //路径
    @Column(name = "rank")
    private int rank;            //排序
    @Column(name = "status")
    private int status;            //状态    （1正常）
    @Column(name = "uploadTime")
    private String uploadTime;        //上传时间
    @Column(name = "terminalName")
    private String terminalName;    //终端（pc、app）

    private static AssetPosterRepository assetPosterRepository;

    public static AssetPosterRepository getAssetPosterRepository() {
        if (null == assetPosterRepository) {
            assetPosterRepository = InstanceFactory.getInstance(AssetPosterRepository.class);
        }
        return assetPosterRepository;
    }

}
