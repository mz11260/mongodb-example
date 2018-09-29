package com.zm.example.service;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.result.UpdateResult;
import com.zm.example.constant.CollectionEnum;
import com.zm.example.entity.User;
import com.zm.example.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.Part;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by Administrator on 2018/9/28.
 */
@Slf4j
@Service
public class UserService {

    private MongoTemplate mongoTemplate;

    /** GridFS 存储桶操作模板 */
    private GridFsTemplate gridFsTemplate;
    private MongoDbFactory dbFactory;
    private UserRepository userRepository;

    @Autowired
    public void setDbFactory(MongoDbFactory dbFactory) {
        this.dbFactory = dbFactory;
    }
    @Autowired
    public void setGridFsTemplate(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }
    @Autowired
    public void setMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 保存用户
     * @param user user
     * @param part 用户头像文件
     * @throws IOException IOException
     */
    public void save(User user, Part part) throws IOException {
        if (part != null) {
            ObjectId objectId = this.store(part);
            user.setAvatar(objectId.toString());
        }
        this.userRepository.save(user);
    }

    /**
     * 一次查询所有
     * @return list
     */
    public List<User> findAll() {
        return this.userRepository.findAll();
    }

    /**
     * 根据名称查询
     * @param name name
     * @return user
     */
    public User findByName(String name) {
        User user = new User();
        user.setName(name);

        Example<User> example = Example.of(user);

        Optional<User> optional = userRepository.findOne(example);

        return optional.orElse(user);
    }

    /**
     * 分页查询，不排序
     * @param pageIndex 第几页
     * @param size 每页几条
     * @param user 查询条件
     */
    public PageImpl<User> findAll(User user, int pageIndex, int size, Sort sort) {
        Example<User> example = Example.of(user);
        Pageable pageable = PageRequest.of(pageIndex, size, sort);
        Page<User> page = userRepository.findAll(example, pageable);

        return new PageImpl<>(page.getContent(), page.getPageable(), page.getTotalElements());
    }

    /**
     * 从Bucket中获取文件
     * @param id objectId
     * @return GridFSFile
     */
    public GridFsResource getAvatarFile(String id) {

        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(id)));
        GridFSDownloadStream gridFSDownloadStream = getGridFs().openDownloadStream(gridFSFile.getObjectId());
        return new GridFsResource(gridFSFile, gridFSDownloadStream);
    }

    private ObjectId store(@NotNull Part part) throws IOException {
        String filename = System.currentTimeMillis() + part.getSubmittedFileName().substring(part.getSubmittedFileName().indexOf("."));
        return gridFsTemplate.store(part.getInputStream(), filename, part.getContentType());
    }

    /**
     * @return GridFSBucket
     */
    private GridFSBucket getGridFs() {
        MongoDatabase db = dbFactory.getDb();
        return GridFSBuckets.create(db);
    }

    /*================= 使用MongoTemplate操作 =================*/

    /**
     * 分页查询，排序，使用mongoTemplate
     * @param query 查询条件
     * @return page
     */
    public Page<User> findAll(Query query, int pageIndex, int size, Sort sort) {
        // long count = mongoTemplate.count(query, CollectionEnum.USER.name());
        // List<User> list = mongoTemplate.find(query, User.class);
        Pageable pageable = PageRequest.of(pageIndex, size, sort);

        long count = mongoTemplate.count(query, User.class);
        if (count > 0) {
            query.with(pageable);
            List<User> list = mongoTemplate.find(query, User.class, CollectionEnum.USER.getValue());
            return PageableExecutionUtils.getPage(list, pageable, () -> count);
        }
        return PageableExecutionUtils.getPage(new ArrayList<>(), pageable, () -> 0L);
    }

    /**
     * 批量更新
     * @param query 查询条件
     * @param update 更新字段
     * @return updateResult
     */
    public UpdateResult batchUpdate(Query query, Update update) {
        return this.mongoTemplate.updateMulti(query, update, User.class);
    }

    public User updateById(Query query, Update update, Part part, String oldAvatar) {
        if (part != null) {
            try {
                if (!StringUtils.isEmpty(oldAvatar)) {
                    gridFsTemplate.delete(Query.query(Criteria.where("_id").is(oldAvatar)));
                    log.info("delete old avatar [{}]", oldAvatar);
                }
                ObjectId objectId = this.store(part);
                update.set("avatar", objectId.toString());
            } catch (IOException e) {
                log.error("保存文件失败", e);
            }
        }
        this.mongoTemplate.updateFirst(query, update, User.class, CollectionEnum.USER.getValue());
        return mongoTemplate.findOne(query, User.class, CollectionEnum.USER.getValue());
    }
}
