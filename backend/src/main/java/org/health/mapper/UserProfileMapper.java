package org.health.mapper;

import org.apache.ibatis.annotations.*;
import org.health.pojo.UserProfile;

@Mapper
public interface UserProfileMapper {
    @Update("CREATE TABLE IF NOT EXISTS user_profile (user_id INT PRIMARY KEY, name VARCHAR(50), avatar MEDIUMTEXT, gender VARCHAR(10), age INT, height DOUBLE, weight DOUBLE)")
    void initialize();

    @Select("SELECT user_id AS userId, name, avatar, gender, age, height, weight FROM user_profile WHERE user_id = #{userId}")
    UserProfile find(Integer userId);

    @Insert("INSERT INTO user_profile (user_id,name,avatar,gender,age,height,weight) VALUES (#{userId},#{name},#{avatar},#{gender},#{age},#{height},#{weight}) ON DUPLICATE KEY UPDATE name=VALUES(name),avatar=VALUES(avatar),gender=VALUES(gender),age=VALUES(age),height=VALUES(height),weight=VALUES(weight)")
    void save(UserProfile profile);
}
