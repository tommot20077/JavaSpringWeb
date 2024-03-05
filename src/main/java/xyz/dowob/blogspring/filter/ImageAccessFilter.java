package xyz.dowob.blogspring.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.dowob.blogspring.Exceptions.Postdata_UpdateException;
import xyz.dowob.blogspring.model.Post;
import xyz.dowob.blogspring.service.PostService;

import java.io.IOException;

@Component
public class ImageAccessFilter implements Filter {

    private final PostService postService;
    private final HttpServletRequest request;
    @Autowired
    public ImageAccessFilter(PostService postService, HttpServletRequest request) {
        this.postService = postService;
        this.request = request;
    }


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
        String requestUrl = httpRequest.getRequestURI();
        Long currentUserId = null;

        if (requestUrl.matches("/extra/\\d+/.*")) {
            String articleId = requestUrl.split("/")[2];

            HttpSession session = request.getSession();
            if (session != null) {
                currentUserId = (Long) session.getAttribute("currentUserId");
            }

            if (isArticleIdSuspendedOrDeleted(articleId, currentUserId)) {
                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }



        }
        filterChain.doFilter(servletRequest, servletResponse);
    }


    private boolean isArticleIdSuspendedOrDeleted(String articleId, Long currentUserId) {
        try {
            Post post = postService.getPostByArticle_id(Long.parseLong(articleId));
            if (post.isDeleted()) {
                return true;
            } else if (!post.isPublished()){
                if (currentUserId != null){
                    return !currentUserId.equals(post.getAuthor().getId());
                } else {
                    return true;
                }
            }
            return false;
        } catch (Postdata_UpdateException e) {
            return false;
        }

    }
}
