package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author fzqqq
 * @create 2020-09-23 16:53
 */
@Data
public class SkuVo extends SkuEntity {
    // 库存
    // 图片
    private List<String> images;

    // 积分活动
    private BigDecimal growBounds;
    private BigDecimal buyBounds;
    /**
     * 优惠生效情况[1111（四个状态位，从右到左）;
     * 0 - 无优惠，成长积分是否赠送;
     * 1 - 无优惠，购物积分是否赠送;
     * 2 - 有优惠，成长积分是否赠送;
     * 3 - 有优惠，购物积分是否赠送【状态位0：不赠送，1：赠送】]
     */
    private List<Integer> work;

    // 满多少 减多少
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    //是否参与其他优惠
    private Integer fullAddOther;

    // 满几件 打几折
    private Integer fullCount;
    private Integer discount;
    /**
     * 是否叠加其他优惠[0-不可叠加，1-可叠加]
     */
    private Integer ladderAddOther;
    private List<SkuAttrValueEntity> saleAttrs;

}
