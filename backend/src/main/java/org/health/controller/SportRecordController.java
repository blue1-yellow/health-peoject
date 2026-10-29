package org.health.controller;

import org.health.pojo.SportRecord;
import org.health.service.SportRecordService;
import org.health.pojo.Result;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/sport")
public class SportRecordController {

    private final SportRecordService sportRecordService;

    public SportRecordController(SportRecordService sportRecordService) {
        this.sportRecordService = sportRecordService;
    }

    //新增运动记录
    @PostMapping
    public Result add(@RequestBody SportRecord sportRecord){
        sportRecordService.add(sportRecord);
        return Result.success();
    }

    //查询用户全部运动记录
    @GetMapping("/list/{userId}")
    public Result<List<SportRecord>> list(@PathVariable Integer userId){
        List<SportRecord> list = sportRecordService.getList(userId);
        return Result.success(list);
    }

    //删除
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id){
        sportRecordService.delete(id);
        return Result.success();
    }

    //修改
    @PutMapping
    public Result update(@RequestBody SportRecord sportRecord){
        sportRecordService.update(sportRecord);
        return Result.success();
    }

    //查询单条
    @GetMapping("/{id}")
    public Result<SportRecord> getOne(@PathVariable Integer id){
        SportRecord record = sportRecordService.getById(id);
        return Result.success(record);
    }
}
