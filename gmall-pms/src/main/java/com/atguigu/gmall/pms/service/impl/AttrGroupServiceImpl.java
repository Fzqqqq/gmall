package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.mapper.SpuAttrValueMapper;
import com.atguigu.gmall.pms.vo.AttrValueVo;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.AttrGroupMapper;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import org.springframework.util.CollectionUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrMapper attrMapper;


    @Autowired
    private SpuAttrValueMapper spuAttrValueMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<AttrGroupEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageResultVo(page);
    }

    //根据三级分类查询
    @Override
    public List<AttrGroupEntity> queryAttrGroupByCid(Long cid) {
        List<AttrGroupEntity> list = this.list(new QueryWrapper<AttrGroupEntity>().eq("category_id", cid));
        return list;
    }

    //添加spu属性信息
    @Override
    public List<AttrGroupEntity> queryByCid(Long catId) {
        // 先查询分组
        List<AttrGroupEntity> groupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("category_id", catId));
        // 在查询每个分组下的规格参数
        if (CollectionUtils.isEmpty(groupEntities)) {
            return null;
        }
        groupEntities.forEach(attrGroupEntity -> {
            List<AttrEntity> attrEntities = attrMapper.selectList(new QueryWrapper<AttrEntity>().eq("group_id", attrGroupEntity.getId()).eq("type", 1));
            attrGroupEntity.setAttrEntities(attrEntities);
        });
        return groupEntities;
    }

    @Override
    public List<ItemGroupVo> queryGroupsBySpuIdAndCid(Long spuId, Long skuId, Long cid3) {
        // 通过cid3  查询 赋值给cateVo
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("category_id", cid3));

        // 遍历attrEntities
        return attrGroupEntities.stream().map(attrGroupEntity -> {
            List<AttrEntity> attrEntities = this.attrMapper.selectList(new QueryWrapper<AttrEntity>()
                    .eq("group_id", attrGroupEntity.getId()));
            ItemGroupVo itemGroupVo = new ItemGroupVo();
            itemGroupVo.setGroupName(attrGroupEntity.getName());

            // 遍历attrEntities 查sku
            ArrayList<AttrValueVo> attrValueVos = new ArrayList<>();
            attrEntities.forEach(attrEntity -> {
                SkuAttrValueEntity skuAttrValueEntity = this.skuAttrValueMapper.selectOne(new QueryWrapper<SkuAttrValueEntity>()
                        .eq("attr_id", attrEntity.getId()).eq("sku_id", skuId));

                if (skuAttrValueEntity != null) {
                    System.out.println("skuAttrValueEntities = " + skuAttrValueEntity);
                    AttrValueVo attrValueVo = new AttrValueVo();
                    BeanUtils.copyProperties(skuAttrValueEntity, attrValueVo);
                    System.out.println("attrValueVo = " + attrValueVo);
                    attrValueVos.add(attrValueVo);
                }
                SpuAttrValueEntity spuAttrValueEntity = this.spuAttrValueMapper.selectOne(new QueryWrapper<SpuAttrValueEntity>()
                        .eq("attr_id", attrEntity.getId()).eq("spu_id", spuId));
                if (spuAttrValueEntity != null) {
                    System.out.println("spuAttrValueEntity = " + spuAttrValueEntity);
                    AttrValueVo attrValueVo = new AttrValueVo();
                    BeanUtils.copyProperties(spuAttrValueEntity, attrValueVo);
                    attrValueVos.add(attrValueVo);
                }
            });
            // 遍历attrEntities 查spu
            itemGroupVo.setAttrValues(attrValueVos);
            return itemGroupVo;
        }).collect(Collectors.toList());
    }
}