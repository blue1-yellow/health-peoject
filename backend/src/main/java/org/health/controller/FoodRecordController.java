package org.health.controller;

import org.health.pojo.FoodRecord;
import org.health.service.FoodRecordService;
import org.health.util.UserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;

@RestController
@RequestMapping("/food")
public class FoodRecordController {

    private final FoodRecordService foodRecordService;

    public FoodRecordController(FoodRecordService foodRecordService) {
        this.foodRecordService = foodRecordService;
    }

    //新增饮食记录
    @PostMapping("/add")
    public String add(@RequestBody FoodRecord foodRecord){
        foodRecord.setUserId(UserContext.getUserId());
        foodRecordService.add(foodRecord);
        return "新增成功";
    }

    //查询全部饮食记录
    @GetMapping("/list")
    public List<FoodRecord> list(){
        return foodRecordService.list(UserContext.getUserId());
    }

    @GetMapping("/{id}")
    public Object get(@PathVariable Integer id) {
        FoodRecord record = foodRecordService.get(id, UserContext.getUserId());
        return record == null ? org.springframework.http.ResponseEntity.notFound().build()
                : org.springframework.http.ResponseEntity.ok(record);
    }

    @PutMapping("/{id}")
    public Object update(@PathVariable Integer id, @RequestBody FoodRecord foodRecord) {
        foodRecord.setId(id);
        foodRecord.setUserId(UserContext.getUserId());
        return foodRecordService.update(foodRecord, UserContext.getUserId())
                ? org.springframework.http.ResponseEntity.ok("更新成功")
                : org.springframework.http.ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public Object delete(@PathVariable Integer id) {
        return foodRecordService.delete(id, UserContext.getUserId())
                ? org.springframework.http.ResponseEntity.ok("删除成功")
                : org.springframework.http.ResponseEntity.notFound().build();
    }
}

