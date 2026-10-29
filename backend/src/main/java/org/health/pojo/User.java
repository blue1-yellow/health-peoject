package org.health.pojo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Integer id;
    private String username;
    private String password;
    private String name;
    private LocalDateTime createTime;

    public Integer getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
}

