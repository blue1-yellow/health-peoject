package org.health.service;

import org.health.mapper.FoodRecordMapper;
import org.health.pojo.FoodRecord;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FoodRecordServiceImpl implements FoodRecordService {

    private final FoodRecordMapper foodRecordMapper;

    public FoodRecordServiceImpl(FoodRecordMapper foodRecordMapper) {
        this.foodRecordMapper = foodRecordMapper;
    }

    @Override
    public void add(FoodRecord foodRecord) {
        foodRecordMapper.insert(foodRecord);
    }

    @Override
    public List<FoodRecord> list(Integer userId) {
        return foodRecordMapper.selectByUserId(userId);
    }

    @Override
    public FoodRecord get(Integer id, Integer userId) {
        return foodRecordMapper.selectByIdAndUserId(id, userId);
    }

    @Override
    public boolean update(FoodRecord foodRecord, Integer userId) {
        return foodRecordMapper.updateByUserId(foodRecord, userId) > 0;
    }

    @Override
    public boolean delete(Integer id, Integer userId) {
        return foodRecordMapper.deleteByIdAndUserId(id, userId) > 0;
    }
}

