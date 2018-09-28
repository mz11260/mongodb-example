package com.zm.example.controller;

import com.zm.example.entity.User;
import com.zm.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Administrator on 2018/9/28.
 */
@RestController
@RequestMapping("user")
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
            e.printStackTrace();
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
        byte[] body = null;
        HttpStatus statusCode = HttpStatus.OK;
        InputStream is = null;
        try {
            //获取文件
            GridFsResource resource = userService.getAvatarFile(id);
            is = resource.getInputStream();
            body = new byte[is.available()];
            is.read(body);
            //设置文件类型
            headers.add("Content-Disposition", "attchement;filename=" + resource.getFilename());
            return new ResponseEntity<>(body, headers, statusCode);
        } catch (Exception e) {
            statusCode = HttpStatus.EXPECTATION_FAILED;
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return new ResponseEntity<>(body, headers, statusCode);
    }
}
