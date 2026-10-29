package org.health.pojo;
import lombok.Data;
import java.util.Date;
@Data
public class FoodRecord {
        private Integer id;
        private Integer userId;
        private String foodName;
        private Date eatTime;
        private Integer cal;
        private String note;
        private Date createTime;
    }

