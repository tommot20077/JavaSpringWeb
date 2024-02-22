package xyz.dowob.blogspring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import xyz.dowob.blogspring.config.UserConfig;

@SpringBootApplication
@EnableScheduling
public class BlogSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogSpringApplication.class, args);
        UserConfig.standardSetupCommand("config.properties");




    }

}
