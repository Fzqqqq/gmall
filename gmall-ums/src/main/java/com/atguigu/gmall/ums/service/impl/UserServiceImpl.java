package com.atguigu.gmall.ums.service.impl;

import com.atguigu.gmall.common.exception.UserException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.ums.mapper.UserMapper;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.ums.service.UserService;


@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public Boolean checkData(String data, Integer type) {
        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();
        switch (type) {
            case 1:
                wrapper.eq("username", data);
                break;
            case 2:
                wrapper.eq("phone", data);
                break;
            case 3:
                wrapper.eq("email", data);
                break;
            default:
                return null;
        }
        // 需不需要用usermapper？？？？
        int one = this.userMapper.selectCount(wrapper);
        return one==0;
    }

    @Override
    public void register(UserEntity userEntity, String code) {
        // 校验短信验证码
        // String cacheCode = this.redisTemplate.opsForValue().get(KEY_PREFIX + userEntity.getPhone());
        // if (!StringUtils.equals(code, cacheCode)) {
        //     return false;
        // }
        // 生成盐
        String salt = StringUtils.replace(UUID.randomUUID().toString(),"-","");
        // 对密码进行加密
        userEntity.setPassword(DigestUtils.md5Hex(salt + userEntity.getPassword()));

        // 设置创建时间等
        userEntity.setCreateTime(new Date());
        userEntity.setLevelId(1l);
        userEntity.setStatus(1);
        userEntity.setIntegration(0);
        userEntity.setGrowth(0);
        userEntity.setNickname(userEntity.getNickname());

        // 添加到数据库
        boolean save = this.save(userEntity);
        // if(b){
        // 注册成功，删除redis中的记录
        // this.redisTemplate.delete(KEY_PREFIX + memberEntity.getPhone());
        // }

    }

    @Override
    public UserEntity queryUser(String loginName, String password) {
        // loginName可能是用户名
        // 先要对密码加密，查询拿到盐
        UserEntity userEntity = this.getOne(new QueryWrapper<UserEntity>()
                .eq("username", loginName)
                .or()
                .eq("phone", loginName)
                .or()
                .eq("email", loginName));
        if (userEntity == null) {
            throw new UserException("账户输入不合法！");
        }
        String salt = userEntity.getSalt();

        // 对密码进行加密
        String passwordMd5 = DigestUtils.md5Hex(salt + password);

        if (!StringUtils.equals(passwordMd5, userEntity.getPassword())) {
            throw new UserException("密码输入错误！");
        }


        return userEntity;
    }

}