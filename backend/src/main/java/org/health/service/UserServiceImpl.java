package org.health.service.impl;

import org.health.mapper.UserMapper;
import org.health.pojo.User;
import org.health.service.UserService;
import org.health.util.JwtUtil;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public void register(User user) {
        // 用户名查重
        User findUser = userMapper.findByUsername(user.getUsername());
        if(findUser != null){
            throw new RuntimeException("用户名已存在");
        }
        // 实际项目建议密码加密，比赛先明文，后续可改成BCrypt
        userMapper.insert(user);
    }

    @Override
    public String login(User user) {
        // 查询用户
        User loginUser = userMapper.findByUsername(user.getUsername());
        if(loginUser == null){
            throw new RuntimeException("用户名不存在");
        }
        // 判断密码
        if(!loginUser.getPassword().equals(user.getPassword())){
            throw new RuntimeException("密码错误");
        }
        // 登录成功，生成token，携带userId
        String token = JwtUtil.generateToken(loginUser.getId());
        return token;
    }
}
