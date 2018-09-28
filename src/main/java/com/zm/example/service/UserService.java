package com.zm.example.service;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.zm.example.entity.User;
import com.zm.example.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.Part;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Created by Administrator on 2018/9/28.
 */
@Slf4j
@Service
public class UserService {


    private GridFsTemplate gridFsTemplate;
    private MongoDbFactory dbFactory;
    @Autowired
    public void setDbFactory(MongoDbFactory dbFactory) {
        this.dbFactory = dbFactory;
    }
    @Autowired
    public void setGridFsTemplate(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }
    private UserRepository userRepository;
    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void save(User user, Part part) throws IOException {
        String filename = System.currentTimeMillis() + part.getSubmittedFileName().substring(part.getSubmittedFileName().indexOf("."));
        ObjectId objectId = gridFsTemplate.store(part.getInputStream(), filename, part.getContentType());

        user.setAvatar(objectId.toString());
        this.userRepository.save(user);
    }

    public List<User> findAll() {
        return this.userRepository.findAll();
    }

    public User findByName(String name) {
        User user = new User();
        user.setName(name);

        Example<User> example = Example.of(user);

        Optional<User> optional = userRepository.findOne(example);

        return optional.orElse(user);
    }

    public GridFsResource getAvatarFile(String id) {

        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(id)));
        GridFSDownloadStream gridFSDownloadStream = getGridFs().openDownloadStream(gridFSFile.getObjectId());
        return new GridFsResource(gridFSFile, gridFSDownloadStream);
    }

    private GridFSBucket getGridFs() {
        MongoDatabase db = dbFactory.getDb();
        return GridFSBuckets.create(db);
    }
}
