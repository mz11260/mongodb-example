package com.zm.example;

import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.bson.types.ObjectId;
import org.junit.Test;

import java.text.SimpleDateFormat;

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

    @Test
    public void test2() {
        ObjectId id = new ObjectId("5baf5512f0bb231c481ac550");
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(id.getDate()));
    }
}
