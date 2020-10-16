package com.atguigu.gmall.index.service;

import com.atguigu.gmall.pms.entity.CategoryEntity;

import java.util.List;

/**
 * @author fzqqq
 * @create 2020-10-09 15:21
 */
public interface IndexService {
    List<CategoryEntity> queryLvl1Categories();


    List<CategoryEntity> queryLvl2CategoriesWithSub(Long pid);

    void testLock();

}
