package com.atguigu.gmall.index.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author fzqqq
 * @create 2020-10-09 15:20
 */
@Controller
public class IndexController {
    @Autowired
    private IndexService indexService;


    @GetMapping
    public String toIndex(Model model) {
        // 加载一级目录
        List<CategoryEntity> categoryEntities = this.indexService.queryLvl1Categories();
        model.addAttribute("categories", categoryEntities);
        // TODO: 加载其他数据
        return "index";
    }


    @GetMapping("index/cates/{pid}")
    @ResponseBody
    public ResponseVo<List<CategoryEntity>> queryLvl2CategoriesWithSub(@PathVariable("pid") Long pid) {
        List<CategoryEntity> categoryEntities = this.indexService.queryLvl2CategoriesWithSub(pid);
        return ResponseVo.ok(categoryEntities);
    }

    @GetMapping("index/testlock")
    @ResponseBody
    public ResponseVo testLock(){
        indexService.testLock();
        return ResponseVo.ok();
    }
}
