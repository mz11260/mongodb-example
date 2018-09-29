package com.zm.example.constant;

/**
 * Created by Administrator on 2018/9/29.
 */
public enum CollectionEnum {

    USER("user"),
    ;

    private String value;
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    CollectionEnum(String value) {
        this.value = value;
    }
}
