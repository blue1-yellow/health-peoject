package org.health.mapper;
import org.health.pojo.FoodRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import java.util.List;
@Mapper
public interface FoodRecordMapper {

        @Insert("insert into food_record(user_id, food_name, eat_time, cal, note) values(#{userId}, #{foodName}, #{eatTime}, #{cal}, #{note})")
        int insert(FoodRecord foodRecord);

        @Select("select id, user_id AS userId, food_name AS foodName, eat_time AS eatTime, cal, note, create_time AS createTime " +
                "from food_record where user_id = #{userId} order by create_time desc")
        List<FoodRecord> selectByUserId(Integer userId);

        @Select("select id, user_id AS userId, food_name AS foodName, eat_time AS eatTime, cal, note, create_time AS createTime " +
                "from food_record where id = #{id} and user_id = #{userId}")
        FoodRecord selectByIdAndUserId(@Param("id") Integer id, @Param("userId") Integer userId);

        @Update("update food_record set food_name = #{record.foodName}, eat_time = #{record.eatTime}, cal = #{record.cal}, note = #{record.note} " +
                "where id = #{record.id} and user_id = #{userId}")
        int updateByUserId(@Param("record") FoodRecord record, @Param("userId") Integer userId);

        @Delete("delete from food_record where id = #{id} and user_id = #{userId}")
        int deleteByIdAndUserId(@Param("id") Integer id, @Param("userId") Integer userId);
    }

