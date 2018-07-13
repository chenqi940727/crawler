package com.yunzhitx.mediacrawler.core.poster.domain;

import com.yunzhitx.cloud.common.model.ddd.InstanceFactory;
import com.yunzhitx.mediacrawler.core.poster.infra.PosterRepository;
import lombok.Data;
import tk.mybatis.mapper.entity.Example;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.HashMap;
import java.util.Map;

/**
 * 代码生成器自动生成
 * Date:2017-9-4 17:07:46
 *
 * @author
 */
@Table(name = "me_poster")
@Data
public class Poster {
    @Column(name = "fd_id")
    private Integer id; // 主键id
    @Column(name = "fd_mediaId")
    private Integer mediaId; // 媒资Id
    @Column(name = "fd_type")
    private String type; // title封面other
    @Column(name = "fd_title")
    private String title; // 海报标题
    @Column(name = "fd_serial")
    private Integer serial; // 顺序
    @Column(name = "fd_small")
    private String small; // 海报地址
    @Column(name = "fd_middle")
    private String middle; // 海报地址
    @Column(name = "fd_large")
    private String large; // 海报地址
    @Column(name = "fd_createDate")
    private String createDate; // 创建时间
    @Column(name = "fd_updateDate")
    private String updateDate; // 创建时间

    @Column(name = "fd_reserved2")
    private String reserved2; // 租户标识

    public void save() {

        getPosterRepository().insert(this);
    }


    private static PosterRepository posterRepository;

    public static PosterRepository getPosterRepository() {
        if (null == posterRepository) {
            posterRepository = InstanceFactory.getInstance(PosterRepository.class);
        }
        return posterRepository;
    }


    public void updateLargeByMediaId(Integer mediaId, String large) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("mediaId", mediaId);
        params.put("large", large);
        getPosterRepository().updateLargeByMediaId(params);
    }

    public Boolean selectPosterExsit(Integer packageMediaId) {
        Example example = new Example(Poster.class);
        example.createCriteria().andEqualTo("mediaId", packageMediaId);
        Integer count = getPosterRepository().selectCountByExample(example);
        if(count == 1){
            return true;
        }else{
            return false;
        }
    }

    public void delete() {
        getPosterRepository().delete(this);
    }
}