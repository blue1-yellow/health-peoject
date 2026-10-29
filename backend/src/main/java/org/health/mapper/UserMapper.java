package org.health.mapper;

import org.health.pojo.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    // 根据用户名查询用户（登录、查重）
    User findByUsername(String username);
    // 新增用户（注册）
    int insert(User user);
}
