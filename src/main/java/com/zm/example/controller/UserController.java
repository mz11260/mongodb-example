package com.zm.example.controller;

import com.mongodb.client.result.UpdateResult;
import com.zm.example.entity.User;
import com.zm.example.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2018/9/28.
 */
@RestController
@RequestMapping("user")
@Slf4j
public class UserController {

    private HttpServletRequest request;
    @Autowired
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }
    private UserService userService;
    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = "saveUser", method = {RequestMethod.HEAD, RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
    public String saveUser(User user) {
        try {
            Part part = request.getPart("file");
            this.userService.save(user, part);
            return "success";
        } catch (Exception e) {
            log.error("保存异常", e);
        }
        return "error";
    }

    @GetMapping("findByName")
    public User findByName(String name) {
        return userService.findByName(name);
    }

    @GetMapping("getAvatarFile/{id}")
    public ResponseEntity<byte[]> getAvatarFile(@PathVariable("id") String id) {
        HttpHeaders headers = new HttpHeaders();
        HttpStatus statusCode = HttpStatus.OK;
        InputStream in = null;
        try {
            //获取文件
            GridFsResource resource = userService.getAvatarFile(id);
            in = resource.getInputStream();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            IOUtils.copy(in, out);

            //设置文件类型
            headers.add("Content-Disposition", "attachment;filename=" + resource.getFilename());

            return new ResponseEntity<>(out.toByteArray(), headers, statusCode);
        } catch (Exception e) {
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
            log.error("获取文件异常", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("关闭流异常", e);
                }
            }
        }
        return new ResponseEntity<>(new byte[]{}, headers, statusCode);
    }

    @GetMapping("getPage")
    public PageImpl<User> getPage(User user, int pageIndex, int size) {

        // 完全匹配Pattern pattern = Pattern.compile("^XXX$", Pattern.CASE_INSENSITIVE);
        // 右匹配Pattern pattern = Pattern.compile("^.*XXX$", Pattern.CASE_INSENSITIVE);
        // 左匹配Pattern pattern = Pattern.compile("^XXX.*$", Pattern.CASE_INSENSITIVE);
        // 模糊匹配Pattern pattern = Pattern.compile("^.*XXX.*$", Pattern.CASE_INSENSITIVE);

        Query query = new Query();
        if (!StringUtils.isEmpty(user.getAddress())) {
            Pattern pattern = Pattern.compile("^.*" + user.getAddress() + ".*$", Pattern.CASE_INSENSITIVE);
            query.addCriteria(Criteria.where("address").regex(pattern));
        }
        return (PageImpl<User>) userService.findAll(query, pageIndex, size, new Sort(Sort.Direction.DESC, "_id"));
    }

    @PostMapping("batchUpdate")
    public UpdateResult batchUpdate(User user) {
        Query query = new Query();
        if (!StringUtils.isEmpty(user.getName())) {
            Pattern pattern = Pattern.compile("^.*" + user.getName() + ".*$", Pattern.CASE_INSENSITIVE);
            query.addCriteria(Criteria.where("name").regex(pattern));
        }
        Update update = new Update();
        update.set("email", user.getEmail());
        return userService.batchUpdate(query, update);
    }

    @PostMapping("updateById")
    public User updateById(User user) {
        Part part = null;
        try {
            part = request.getPart("file");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Query query = Query.query(Criteria.where("_id").is(user.getId()));
        Update update = new Update();
        if (!StringUtils.isEmpty(user.getName())) {
            update.set("name", user.getName());
        }
        if (!StringUtils.isEmpty(user.getAddress())) {
            update.set("address", user.getAddress());
        }
        if (!StringUtils.isEmpty(user.getEmail())) {
            update.set("email", user.getEmail());
        }
        if (!StringUtils.isEmpty(user.getPhone())) {
            update.set("phone", user.getPhone());
        }
        if (!StringUtils.isEmpty(user.getAge())) {
            update.set("age", user.getAge());
        }
        if (!StringUtils.isEmpty(user.getSex())) {
            update.set("sex", user.getSex());
        }
        update.set("_class", User.class.getName());
        return userService.updateById(query, update, part, user.getAvatar());
    }

}
