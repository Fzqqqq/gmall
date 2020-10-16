package com.atguigu.gmall.search.pojo;

import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import lombok.Data;

import java.util.List;

/**
 * @author fzqqq
 * @create 2020-09-29 10:54
 */
@Data
public class SearchResponseVo {
    // 封装品牌过滤条件
    private List<BrandEntity> brands;
    // 封装分类过滤条件
    private List<CategoryEntity> categoryEntities;
    // 封装规格参数过滤条件
    private List<SearchResponseAttrValueVo> filters;

    // 分页数据
    private Integer pageNum;
    private Integer pageSize;
    private Long total;
    private List<Goods> data;


}
