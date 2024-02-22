package xyz.dowob.blogspring.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        UserConfig userConfig = UserConfig.standardSetupCommand("config.properties");
        String dirPath = userConfig.getImagePath();
        String location = "file:" + dirPath + "/";
        registry.addResourceHandler("/extra/**")
                .addResourceLocations(location);
    }
}