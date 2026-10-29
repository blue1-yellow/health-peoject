package org.health.mapper;

import org.health.pojo.MoodRecord;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface MoodRecordMapper {
    int insert(MoodRecord moodRecord);
    List<MoodRecord> selectByUserId(Integer userId);
    int deleteById(Integer id);
    int update(MoodRecord moodRecord);
    MoodRecord selectById(Integer id);
}
