package com.atguigu.gmall.pms.controller;

import java.util.List;

import com.atguigu.gmall.pms.entity.SpuEntity;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * 属性分组
 *
 * @author canglx
 * @email canglx1996@163.com
 * @date 2020-09-21 18:52:03
 */
@Api(tags = "属性分组 管理")
@RestController
@RequestMapping("pms/attrgroup")
public class AttrGroupController {

    @Autowired
    private AttrGroupService attrGroupService;

    @GetMapping("withattrvalues")
    public ResponseVo<List<ItemGroupVo>> queryGroupsBySpuIdAndCid(@RequestParam("spuId") Long spuId,
                                                                  @RequestParam("skuId") Long skuId,
                                                                  @RequestParam("cid3") Long cid3) {
        List<ItemGroupVo> itemGroupVOS = attrGroupService.queryGroupsBySpuIdAndCid(spuId, skuId, cid3);
        return ResponseVo.ok(itemGroupVOS);

    }

    @GetMapping("withattrs/{catId}")
    @ApiOperation("添加spu属性信息")
    public ResponseVo<List<AttrGroupEntity>> queryByCid(@PathVariable("catId") Long catId) {
        List<AttrGroupEntity> attrGroupEntities = attrGroupService.queryByCid(catId);
        return ResponseVo.ok(attrGroupEntities);

    }

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> queryAttrGroupByPage(PageParamVo paramVo) {
        PageResultVo pageResultVo = attrGroupService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }

    @GetMapping("category/{cid}")
    @ApiOperation("根据三级分类查询")
    public ResponseVo<List<AttrGroupEntity>> queryAttrGroupByCid(@PathVariable("cid") Long cid) {
        List<AttrGroupEntity> groupEntities = attrGroupService.queryAttrGroupByCid(cid);
        return ResponseVo.ok(groupEntities);
    }

    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<AttrGroupEntity> queryAttrGroupById(@PathVariable("id") Long id) {
        AttrGroupEntity attrGroup = attrGroupService.getById(id);

        return ResponseVo.ok(attrGroup);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids) {
        attrGroupService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
