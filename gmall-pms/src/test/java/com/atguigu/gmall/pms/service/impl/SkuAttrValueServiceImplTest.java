package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.service.SkuAttrValueService;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author fzqqq
 * @create 2020-10-13 18:45
 */
@SpringBootTest
class SkuAttrValueServiceImplTest {

    @Autowired
    private SkuAttrValueService skuAttrValueService;

    @Test
    void querySkuAttrValuesBySpuId() {
        List<SaleAttrValueVo> saleAttrValueVos = skuAttrValueService.querySkuAttrValuesBySpuId(7l);
        System.out.println("saleAttrValueVos = " + saleAttrValueVos);
    }
}