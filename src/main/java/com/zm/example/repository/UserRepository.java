package com.zm.example.repository;

import com.zm.example.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by Administrator on 2018/9/28.
 */
public interface UserRepository extends MongoRepository<User, String> {
}
