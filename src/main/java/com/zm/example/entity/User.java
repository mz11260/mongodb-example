package com.zm.example.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2018/9/28.
 */
@Data
@NoArgsConstructor
@ToString
@Document(collection = "user")
public class User {
    @Id
    private String id;
    private String name;
    private String email;
    private String phone;
    private String address;
    /** 头像 */
    private String avatar;
    private Integer age;
    private Integer sex;

}
