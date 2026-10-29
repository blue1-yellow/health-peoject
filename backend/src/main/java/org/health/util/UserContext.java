package org.health.util;

public class UserContext {
    // ThreadLocal，每个请求独立存储userId
    private static ThreadLocal<Integer> userId = new ThreadLocal<>();

    public static void setUserId(Integer id){
        userId.set(id);
    }

    public static Integer getUserId(){
        return userId.get();
    }

    // 请求结束必须清除，防止内存泄漏
    public static void remove(){
        userId.remove();
    }
}
