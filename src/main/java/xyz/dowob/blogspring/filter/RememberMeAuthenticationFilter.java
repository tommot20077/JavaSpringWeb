package xyz.dowob.blogspring.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import xyz.dowob.blogspring.model.User;
import xyz.dowob.blogspring.service.UserService;

import java.io.IOException;

@Component
public class RememberMeAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private UserService userService;
    @Autowired
    private CacheManager cacheManager;



    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        User user = null;
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUserId") == null) {
            Cookie[] cookies = request.getCookies();
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("REMEMBER_ME")) {
                    Long userId = userService.verifyRememberMeToken(cookie.getValue());
                    if (userId != null) {
                        Cache userCache = cacheManager.getCache("user");
                        if (userCache != null) {
                            user = userCache.get(userId, User.class);
                            if (user == null) {
                                user = userService.getUserById(userId);
                                userCache.put(userId, user);
                            }
                        }
                        if (user != null) {
                            session = request.getSession(true);
                            session.setAttribute("currentUsername", user.getUsername());
                            session.setAttribute("currentUserId", user.getId());
                            session.setAttribute("currentUserEmailStatus", user.getEmailActiveStatus());
                        }
                    }
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
