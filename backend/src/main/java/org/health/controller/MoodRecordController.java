package org.health.controller;

import org.health.pojo.MoodRecord;
import org.health.service.MoodRecordService;
import org.health.pojo.Result;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/mood")
public class MoodRecordController {

    private final MoodRecordService moodRecordService;

    public MoodRecordController(MoodRecordService moodRecordService) {
        this.moodRecordService = moodRecordService;
    }

    //新增情绪记录
    @PostMapping
    public Result add(@RequestBody MoodRecord moodRecord){
        moodRecordService.add(moodRecord);
        return Result.success();
    }

    //查询用户全部情绪记录
    @GetMapping("/list/{userId}")
    public Result<List<MoodRecord>> list(@PathVariable Integer userId){
        List<MoodRecord> list = moodRecordService.getList(userId);
        return Result.success(list);
    }

    //删除
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id){
        moodRecordService.delete(id);
        return Result.success();
    }

    //修改
    @PutMapping
    public Result update(@RequestBody MoodRecord moodRecord){
        moodRecordService.update(moodRecord);
        return Result.success();
    }

    //查询单条
    @GetMapping("/{id}")
    public Result<MoodRecord> getOne(@PathVariable Integer id){
        MoodRecord record = moodRecordService.getById(id);
        return Result.success(record);
    }
}
