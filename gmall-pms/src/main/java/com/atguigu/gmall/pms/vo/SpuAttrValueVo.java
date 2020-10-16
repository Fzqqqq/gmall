package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author fzqqq
 * @create 2020-09-23 11:38
 */
public class SpuAttrValueVo extends SpuAttrValueEntity {


    public void setValueSelected(List<Object> valueSelected) {
        if (CollectionUtils.isEmpty(valueSelected)) {
            return;
        }
        this.setAttrValue(StringUtils.join(valueSelected, ","));
    }
}
