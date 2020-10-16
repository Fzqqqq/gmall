package com.atguigu.gmall.pms.vo;

import lombok.Data;

import java.util.Set;

/**
 * @author fzqqq
 * @create 2020-10-12 18:25
 */
@Data
public class SaleAttrValueVo {

    private Long attrId;
    private String attrName;
    private Set<String> attrValues;
}
