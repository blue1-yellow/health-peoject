package org.health.controller;

import org.health.pojo.Result;
import org.health.pojo.User;
import org.health.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 注册接口
    @PostMapping("/register")
    public Result register(@RequestBody User user){
        userService.register(user);
        return Result.success("注册成功");
    }

    // 登录接口，返回token
    @PostMapping("/login")
    public Result<String> login(@RequestBody User user){
        String token = userService.login(user);
        return Result.success(token);
    }
    @GetMapping("/info")
    public String userInfo(){
        return "用户信息接口成功访问";
    }
}
