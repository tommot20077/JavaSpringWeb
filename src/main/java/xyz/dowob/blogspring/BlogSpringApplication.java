package xyz.dowob.blogspring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import xyz.dowob.blogspring.config.UserConfig;
import xyz.dowob.blogspring.functions.StorageMethod;

import java.io.File;

@SpringBootApplication
@EnableScheduling
public class BlogSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogSpringApplication.class, args);


        String jarPath = StorageMethod.getRunningDirectory();
        String configPath = jarPath + File.separator + "config.properties";
        new UserConfig(configPath);
    }

}
