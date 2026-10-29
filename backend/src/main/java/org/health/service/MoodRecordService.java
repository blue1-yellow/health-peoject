package org.health.service;

import org.health.pojo.MoodRecord;
import java.util.List;

public interface MoodRecordService {
    void add(MoodRecord moodRecord);
    List<MoodRecord> getList(Integer userId);
    void delete(Integer id);
    void update(MoodRecord moodRecord);
    MoodRecord getById(Integer id);
}
