package org.health.service;

import org.health.pojo.SportRecord;
import java.util.List;

public interface SportRecordService {
    void add(SportRecord sportRecord);
    List<SportRecord> getList(Integer userId);
    void delete(Integer id);
    void update(SportRecord sportRecord);
    SportRecord getById(Integer id);
}
