package org.health.service;

import org.health.pojo.User;

public interface UserService {
    // 注册
    void register(User user);
    // 登录，成功返回JWT令牌字符串
    String login(User user);
}
