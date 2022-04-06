package ru.alta.thirdproj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class ThirdprojApplication  {

    public static void main(String[] args) {
//        new SpringApplicationBuilder(ThirdprojApplication.class)
//                .web(WebApplicationType.NONE)
//                .run(args);
        SpringApplication.run(ThirdprojApplication.class, args);
    }

}
