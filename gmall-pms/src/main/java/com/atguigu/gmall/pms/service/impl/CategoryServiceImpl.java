package com.atguigu.gmall.pms.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.CategoryMapper;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, CategoryEntity> implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<CategoryEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<CategoryEntity> queryCategoryByParentId(Long parentId) {
        QueryWrapper<CategoryEntity> wrapper = new QueryWrapper<>();
        if (parentId != -1) {
            wrapper.eq("parent_id", parentId);
        }
        List<CategoryEntity> categoryEntities = this.list(wrapper);
        return categoryEntities;
    }

    //查询二级三级分类
    @Override
    public List<CategoryEntity> queryCategoriesWithSub(Long pid) {


        return this.categoryMapper.queryCategoriesByPid(pid);
    }

    // 根据3级分类id查询123级分类
    @Override
    public List<CategoryEntity> queryCategoriesByCid3(Long cid3) {
        CategoryEntity categoryEntity3 = this.categoryMapper.selectById(cid3);
        CategoryEntity categoryEntity2 = this.categoryMapper.selectById(categoryEntity3.getParentId());
        CategoryEntity categoryEntity1 = this.categoryMapper.selectById(categoryEntity2.getParentId());


        return Arrays.asList(categoryEntity1, categoryEntity2, categoryEntity3);
    }

}