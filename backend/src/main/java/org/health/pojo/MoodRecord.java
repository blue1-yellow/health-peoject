package org.health.pojo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MoodRecord {
    private Integer id;
    private Integer userId;         // 用户id，区分是谁的记录（必加！）
    private Integer moodScore;      // 情绪分数：1~10分
    private String moodDesc;        // 简短情绪描述（开心/焦虑/疲惫）
    private LocalDateTime recordTime;//记录时间
    private String remark;          // 详细备注，心理感受

    public void setUserId(Integer userId) { this.userId = userId; }
}


