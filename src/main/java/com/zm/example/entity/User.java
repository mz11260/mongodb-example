package com.zm.example.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Created by Administrator on 2018/9/28.
 */
@Data
@NoArgsConstructor
@ToString
public class User {
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
