package com.atguigu.gmall.pms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.vo.AttrValueVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import org.springframework.util.CollectionUtils;


@Service("skuAttrValueService")
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValueEntity> implements SkuAttrValueService {


    @Autowired
    private AttrMapper attrMapper;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;


    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuAttrValueEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<SkuAttrValueEntity> querySearchSkuAttrValuesByCidAndSkuId(Long cid, Long skuId) {
        List<AttrEntity> attrEntities = attrMapper.selectList(new QueryWrapper<AttrEntity>().eq("category_id", cid).eq("search_type", 1));
        if (CollectionUtils.isEmpty(attrEntities)) {
            return null;
        }
        // 获取检索规格参数id
        List<Long> attrIds = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());

        // 根据skuId和attrIds查询检索类型的规格参数和值
        List<SkuAttrValueEntity> skuAttrValueEntities = this.list(new QueryWrapper<SkuAttrValueEntity>().eq("sku_id", skuId).in("attr_id", attrIds));


        return skuAttrValueEntities;
    }

    @Override
    public List<SaleAttrValueVo> querySkuAttrValuesBySpuId(Long spuId) {

        List<AttrValueVo> attrValueVos = this.skuAttrValueMapper.querySaleAttrValuesBySpuId(spuId);
        if (!CollectionUtils.isEmpty(attrValueVos)) {
            Map<Long, List<AttrValueVo>> map = attrValueVos.stream().collect(Collectors.groupingBy(AttrValueVo::getAttrId));
            System.out.println("map = " + map);
            List<SaleAttrValueVo> saleAttrValueVos = new ArrayList<>();
            map.forEach((attrId, attrValueEntities) -> {
                SaleAttrValueVo saleAttrValueVo = new SaleAttrValueVo();
                saleAttrValueVo.setAttrId(attrId);
                saleAttrValueVo.setAttrName(attrValueEntities.get(0).getAttrName());
                saleAttrValueVo.setAttrValues(attrValueEntities.stream().map(AttrValueVo::getAttrValue).collect(Collectors.toSet()));
                saleAttrValueVos.add(saleAttrValueVo);
            });
            return saleAttrValueVos;
        }

        // 以attrId进行分组
        return null;
    }

    @Override
    public String querySkusJsonBySpuId(Long spuId) {

        List<Map<String, Object>> skus = this.skuAttrValueMapper.querySkusJsonBySpuId(spuId);
        Map<String, Long> map = skus.stream().collect(Collectors.toMap(sku -> sku.get("attr_values").toString(), sku -> (Long) sku.get("sku_id")));
        return JSON.toJSONString(map);

    }

    @Override
    public List<SkuAttrValueEntity> querySkuAttrValuesBySkuId(Long skuId) {
        List<SkuAttrValueEntity> skuAttrValueEntities = this.list(new QueryWrapper<SkuAttrValueEntity>().eq("sku_id", skuId));
//        List<AttrValueVo> attrValue = skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
//            AttrValueVo attrValueVo = new AttrValueVo();
//            BeanUtils.copyProperties(skuAttrValueEntity, attrValueVo);
//            return attrValueVo;
//        }).collect(Collectors.toList());
        return skuAttrValueEntities;
    }
}