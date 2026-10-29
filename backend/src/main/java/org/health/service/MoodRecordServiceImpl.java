package org.health.service.impl;

import org.health.mapper.MoodRecordMapper;
import org.health.pojo.MoodRecord;
import org.health.service.MoodRecordService;
import org.health.util.UserContext;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MoodRecordServiceImpl implements MoodRecordService {

    private final MoodRecordMapper moodRecordMapper;

    public MoodRecordServiceImpl(MoodRecordMapper moodRecordMapper) {
        this.moodRecordMapper = moodRecordMapper;
    }

    @Override
    public List<MoodRecord> getList(Integer userId) {
        return moodRecordMapper.selectByUserId(userId);
    }

    @Override
    public void delete(Integer id) {
        moodRecordMapper.deleteById(id);
    }

    @Override
    public void update(MoodRecord moodRecord) {
        moodRecordMapper.update(moodRecord);
    }

    @Override
    public MoodRecord getById(Integer id) {
        return moodRecordMapper.selectById(id);
    }

    @Override
    public void add(MoodRecord moodRecord) {
        // 从上下文拿登录用户id，前端不用传
        moodRecord.setUserId(UserContext.getUserId());
        moodRecordMapper.insert(moodRecord);
    }

}
