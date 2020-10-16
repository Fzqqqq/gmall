package com.atguigu.gmall.ums.mapper;

import com.atguigu.gmall.ums.entity.UserLoginLogEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户登陆记录表
 *
 * @author canglx
 * @email canglx1996@163.com
 * @date 2020-09-21 19:52:01
 */
@Mapper
public interface UserLoginLogMapper extends BaseMapper<UserLoginLogEntity> {

}
