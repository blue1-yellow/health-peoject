package org.health.service.impl;

import org.health.mapper.SportRecordMapper;
import org.health.pojo.SportRecord;
import org.health.service.SportRecordService;
import org.health.util.UserContext;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SportRecordServiceImpl implements SportRecordService {

    private final SportRecordMapper sportRecordMapper;

    public SportRecordServiceImpl(SportRecordMapper sportRecordMapper) {
        this.sportRecordMapper = sportRecordMapper;
    }

    @Override
    public List<SportRecord> getList(Integer userId) {
        return sportRecordMapper.selectByUserId(userId);
    }

    @Override
    public void delete(Integer id) {
        sportRecordMapper.deleteById(id);
    }

    @Override
    public void update(SportRecord sportRecord) {
        sportRecordMapper.update(sportRecord);
    }

    @Override
    public SportRecord getById(Integer id) {
        return sportRecordMapper.selectById(id);
    }

    @Override
    public void add(SportRecord sportRecord) {
        // 从上下文拿登录用户id，前端不用传
        sportRecord.setUserId(UserContext.getUserId());
        sportRecordMapper.insert(sportRecord);
    }

}
