package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.service.AttrGroupService;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author fzqqq
 * @create 2020-10-13 0:28
 */

@SpringBootTest
class AttrGroupServiceImplTest {

    @Autowired
    private AttrGroupService attrGroupService;

    @Test
    void queryGroupsBySpuIdAndCid() {
        List<ItemGroupVo> itemGroupVos = attrGroupService.queryGroupsBySpuIdAndCid(7l, 1l, 225l);
        System.out.println("itemGroupVos = " + itemGroupVos);
    }
}