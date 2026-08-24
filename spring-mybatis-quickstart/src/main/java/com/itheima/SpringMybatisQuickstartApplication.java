package com.itheima;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ServletComponentScan("com.itheima.config")
public class SpringMybatisQuickstartApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(SpringMybatisQuickstartApplication.class, args);
    }


   @Override
   protected  SpringApplicationBuilder  configure (SpringApplicationBuilder  builder){

//        return super.configure(builder);
       return builder.sources(SpringMybatisQuickstartApplication.class);
}



}
