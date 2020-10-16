package com.atguigu.gmall.search.pojo;

/**
 * @author fzqqq
 * @create 2020-09-28 20:22
 */

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * 接受页面传递过来的检索参数
 * ?keyword=手机&brandId=1,3&cid3=225,250&props=5:高通-麒麟&props=6:骁龙865-硅谷1000&sort=1&priceFrom=1000&priceTo=6000&pageNum=1&store=true
 *
 */
@Data
@ToString
public class SearchParamVo {
    // 搜索关键字
    private String keyword;
    // 接受品牌id的过滤条件
    private List<Long> brandId;

    // 接受分类的过滤条件
    private List<Long> cid3;

    // 接受规格参数的过滤条件 5:128G-256G-512G
    private List<String> props;

    // 排序 1价格升序  2价格降序  3 新品降序  4 销量降序
    private Integer sort;

    // 价格区间
    private Double priceFrom;
    private Double priceTo;

    // 是否有货
    private Boolean store;

    // 分页数据
    private Long total;
    private Integer pageNum = 1;
    private final Integer pageSize = 20;
    
}
