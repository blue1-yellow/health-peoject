package org.health.mapper;

import org.health.pojo.SportRecord;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface SportRecordMapper {
    int insert(SportRecord sportRecord);
    List<SportRecord> selectByUserId(Integer userId);
    int deleteById(Integer id);
    int update(SportRecord sportRecord);
    SportRecord selectById(Integer id);
}


