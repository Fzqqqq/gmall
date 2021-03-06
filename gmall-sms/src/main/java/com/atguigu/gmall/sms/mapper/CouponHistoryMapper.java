package com.atguigu.gmall.sms.mapper;

import com.atguigu.gmall.sms.entity.CouponHistoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券领取历史记录
 *
 * @author canglx
 * @email canglx1996@163.com
 * @date 2020-09-21 20:06:18
 */
@Mapper
public interface CouponHistoryMapper extends BaseMapper<CouponHistoryEntity> {

}
