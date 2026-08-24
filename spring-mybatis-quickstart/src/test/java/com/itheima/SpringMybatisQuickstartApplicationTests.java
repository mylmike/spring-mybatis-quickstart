package com.itheima;

import com.itheima.mapper.primary.UserMapper;
import com.itheima.pojo.user;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import java.util.List;

@SpringBootTest
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
class SpringMybatisQuickstartApplicationTests {
    @Autowired
     private UserMapper userMapper;
    @Test
    void contextLoads() {
       List<user>  userList= userMapper.list();
       userList.stream().forEach(user -> {
           System.out.println(user.getName());
           System.out.println(user.getLoginid());
           System.out.println(user.getZb());
       });

    }

}
