package com.atguigu.gmall.search.pojo;

import lombok.Data;

import java.util.List;

/**
 * @author fzqqq
 * @create 2020-09-29 10:57
 */
@Data
public class SearchResponseAttrValueVo {
    private Long attrId;
    private String attrName;
    private List<String> attrValues;

}
