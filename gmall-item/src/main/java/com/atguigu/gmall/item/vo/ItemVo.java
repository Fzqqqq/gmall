package com.atguigu.gmall.item.vo;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author fzqqq
 * @create 2020-10-12 18:05
 */
@Data
public class ItemVo {
    // 三级分类
    private List<CategoryEntity> categoryEntities;
    // 品牌
    private Long brandId;
    private String brandName;

    // spu
    private Long spuId;
    private String spuName;

    // sku
    private Long skuId;
    private String title;
    private String subTitle;
    private BigDecimal price;
    private Integer weight;
    private String defaultImage;

    // sku图片
    private List<SkuImagesEntity> images;

    // 营销信息
    private List<ItemSaleVo> sales;

    // 是否有货
    private Boolean store = false;

    // sku 所属的spu所有的销售属性
    // [{attrId: 3, attrName: '颜色', attrValues: '白色','黑色','粉色'},
    // {attrId: 8, attrName: '内存', attrValues: '6G','8G','12G'},
    // {attrId: 9, attrName: '存储', attrValues: '128G','256G','512G'}]
    private List<SaleAttrValueVo> saleAttrs;

    // 当前sku的销售属性:{3:'白色',8:'8G',9:'128G'}
    private Map<Long, String> saleAttr;

    // sku列表：{'白色,8G,128G': 4, '白色,8G,256G': 5, '白色,8G,512G': 6, '白色,12G,128G': 7}   销售属性组合和skuId映射关系
    private String skusJson;

    // 商品描述
    private List<String> spuImages;
    private List<ItemGroupVo> groups;


    /**
     * 已知条件：skuId 需要获取数据模型：ItemVo。需要远程接口：
     * 	1.根据skuId查询sku信息 Y
     * 	2.根据三级分类Id查询一二三级分类Y
     * 	3.根据brandId查询brandY
     * 	4.根据spuId查询spuY
     * 	5.根据skuId查询sku的图片列表Y
     * 	6.根据skuId查询sku所有的营销信息（sms）Y
     * 	7.根据skuId查询库存信息Y
     * 	8.根据spuId查询spu下所有sku的销售属性Y
     * 	9.根据skuId查询sku的销售属性
     * 	10.根据spuId查询spu下所有sku的销售属性组合和skuId的映射关系
     * 	11.根据spuId查询商品描述信息
     * 	12.根据categoryId、spuId、skuId查询组及组下的规格参数和值
     */

}
