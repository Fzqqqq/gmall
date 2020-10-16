package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.vo.SpuVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SpuDescMapper;
import com.atguigu.gmall.pms.entity.SpuDescEntity;
import com.atguigu.gmall.pms.service.SpuDescService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("spuDescService")
public class SpuDescServiceImpl extends ServiceImpl<SpuDescMapper, SpuDescEntity> implements SpuDescService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuDescEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuDescEntity>()
        );

        return new PageResultVo(page);
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSpuDesc(SpuVo spuVo, Long spuId) {
        if (!CollectionUtils.isEmpty(spuVo.getSpuImages())) {
            SpuDescEntity spuDescEntity = new SpuDescEntity();
            // 注意：spu_info_desc表的主键是spu_id,需要在实体类中配置该主键不是自增主键
            spuDescEntity.setSpuId(spuId);
            // 把商品的图片描述，保存到spu详情中，图片地址以逗号进行分割
            spuDescEntity.setDecript(StringUtils.join(spuVo.getSpuImages(), ","));
            this.save(spuDescEntity);
        }
    }
}