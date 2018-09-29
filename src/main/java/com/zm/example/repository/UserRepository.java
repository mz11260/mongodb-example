package com.zm.example.repository;

import com.zm.example.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

/**
 * Created by Administrator on 2018/9/28.
 */
public interface UserRepository extends MongoRepository<User, String> {

    @Query("{ 'age': { $gt: ?0, $lt: ?1 } }")
    List<User> findUsersByAgeBetween(Integer ageGt, Integer ageLt);
}
