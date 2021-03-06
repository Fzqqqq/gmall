package com.atguigu.gmall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author canglx
 * @email canglx1996@163.com
 * @date 2020-09-21 18:52:03
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<CategoryEntity> queryCategoryByParentId(Long parentId);

    //查询二级三级分类
    List<CategoryEntity> queryCategoriesWithSub(Long pid);

    List<CategoryEntity> queryCategoriesByCid3(Long cid3);
}

