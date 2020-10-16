package com.atguigu.gmall.oms.mapper;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 *
 * @author canglx
 * @email canglx1996@163.com
 * @date 2020-09-21 19:28:11
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderEntity> {

}
