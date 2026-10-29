package org.health.service;

import org.health.pojo.FoodRecord;
import java.util.List;

public interface FoodRecordService {
    void add(FoodRecord foodRecord);
    List<FoodRecord> list(Integer userId);
    FoodRecord get(Integer id, Integer userId);
    boolean update(FoodRecord foodRecord, Integer userId);
    boolean delete(Integer id, Integer userId);
}
