package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author canglx
 * @email canglx1996@163.com
 * @date 2020-09-21 18:52:03
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<AttrGroupEntity> queryAttrGroupByCid(Long cid);

    List<AttrGroupEntity> queryByCid(Long catId);

    List<ItemGroupVo> queryGroupsBySpuIdAndCid(Long spuId, Long skuId, Long cid3);
}

