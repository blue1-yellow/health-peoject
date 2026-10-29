package org.health.pojo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SportRecord {
        private Integer id;
        private Integer userId;         // 用户id，区分记录归属
        private String sportName;       // 运动名称
        private Integer duration;       // 时长（分钟）
        private Double calorie;         // 消耗卡路里
        private LocalDateTime recordTime;//记录时间
        private String remark;          // 备注

        public void setUserId(Integer userId) { this.userId = userId; }
}
