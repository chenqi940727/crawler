package com.yunzhitx.mediacrawler.core.haokan.infra;


import com.yunzhitx.mediacrawler.core.haokan.domain.Asset;
import com.yunzhitx.mediacrawler.utils.MyBatisBaseMapper;

import java.util.List;

/**
* 代码生成器自动生成
* Date:2017-9-4 17:07:46
* @author
*/
public interface AssetRepository extends MyBatisBaseMapper<Asset> {
    List<Asset> selectAllTiYu();

    List<Asset> selectAllYWYHK();

    List<Asset> selectAllTV(String videoName);
}