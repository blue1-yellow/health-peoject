package org.health.pojo;

import lombok.Data;

@Data
public class UserProfile {
    private Integer userId;
    private String name;
    private String avatar;
    private String gender;
    private Integer age;
    private Double height;
    private Double weight;
    private Double bmi;
    private Double bodyFat;
}
