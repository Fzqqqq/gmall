package com.atguigu.gmall.ums.mapper;

import com.atguigu.gmall.ums.entity.UserEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表
 *
 * @author canglx
 * @email canglx1996@163.com
 * @date 2020-09-21 19:52:01
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {

}
