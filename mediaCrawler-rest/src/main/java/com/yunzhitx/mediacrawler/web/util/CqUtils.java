package com.yunzhitx.mediacrawler.web.util;

/**
 * @author admin$
 * @Description: TODO
 * @date 2018/5/30$ 17:31$
 */
public class CqUtils {


    public static Integer getPercent(String pointNumber){
        Double value = Double.valueOf(pointNumber) * 100.00;
        return value.intValue();
    }
}
