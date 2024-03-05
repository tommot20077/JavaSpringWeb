package xyz.dowob.blogspring.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import xyz.dowob.blogspring.filter.ImageAccessFilter;
import xyz.dowob.blogspring.filter.RememberMeAuthenticationFilter;
import xyz.dowob.blogspring.service.PostService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final PostService postService;
    private final HttpServletRequest httpRequest;
    @Autowired
    public SecurityConfig(PostService postService, HttpServletRequest httpRequest) {
        this.postService = postService;
        this.httpRequest = httpRequest;
    }



    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        HttpSessionCsrfTokenRepository csrfTokenRepository = new HttpSessionCsrfTokenRepository();

        http
                .csrf((csrf) -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                )


                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionConcurrency((concurrency) -> concurrency
                                .maximumSessions(1)
                                .maxSessionsPreventsLogin(false)
                                .expiredSessionStrategy(event -> event.getResponse().sendRedirect("/login"))
                        )
                )
                .addFilterBefore(rememberMeAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public FilterRegistrationBean<ImageAccessFilter> registerImageAccessFilter(){
        FilterRegistrationBean<ImageAccessFilter> ImageAccessBean = new FilterRegistrationBean<>();
        ImageAccessBean.setFilter(new ImageAccessFilter(postService, httpRequest));
        ImageAccessBean.addUrlPatterns("/extra/*");
        return ImageAccessBean;
    }



    @Bean
    public RememberMeAuthenticationFilter rememberMeAuthenticationFilter() {
        return new RememberMeAuthenticationFilter();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
}
