package xyz.dowob.blogspring.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        UserConfig userConfig = new UserConfig("config.properties");

        String dirPath = userConfig.getImagePath();
        String location = "file:" + dirPath + "/";
        System.out.println("location: " + location);
        // 映射所有访问 "extra" 路径的资源请求到文件系统上的 "extra-resources" 目录
        registry.addResourceHandler("/extra/**")
                .addResourceLocations(location);
    }
}