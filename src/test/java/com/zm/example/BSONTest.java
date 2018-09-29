package com.zm.example;

import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.bson.types.ObjectId;
import org.junit.Test;

/**
 * Created by Administrator on 2018/9/29.
 */
public class BSONTest {

    @Test
    public void test1() {
        BSONObject object = new BasicBSONObject();
        ObjectId id = new ObjectId();
        object.put("_id", id);
        object.put("type", "2");
        System.out.println(object.toString());
    }
}
