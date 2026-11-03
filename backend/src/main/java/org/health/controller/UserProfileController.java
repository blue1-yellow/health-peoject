package org.health.controller;

import org.health.pojo.Result;
import org.health.pojo.UserProfile;
import org.health.service.UserProfileService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/profile")
public class UserProfileController {
    private final UserProfileService service;
    public UserProfileController(UserProfileService service) { this.service = service; }
    @GetMapping
    public Result<UserProfile> get() { return Result.success(service.get()); }
    @PutMapping
    public Result<UserProfile> save(@RequestBody UserProfile profile) { return Result.success(service.save(profile)); }
}
