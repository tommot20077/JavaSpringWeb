package xyz.dowob.blogspring.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import xyz.dowob.blogspring.Exceptions.Postdata_UpdateException;
import xyz.dowob.blogspring.functions.ArticleDto;
import xyz.dowob.blogspring.functions.ImageDto;
import xyz.dowob.blogspring.functions.PublishRequestDto;
import xyz.dowob.blogspring.model.Post;
import xyz.dowob.blogspring.model.User;
import xyz.dowob.blogspring.service.PostService;
import xyz.dowob.blogspring.service.UserService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static xyz.dowob.blogspring.functions.FormattedTimeForUser.formattedTimeForUser;

@Controller


public class PostController {

    private final PostService postService;
    private final UserService userService;

@Autowired
    public PostController(PostService postService, UserService userService) {
        this.postService = postService;

    this.userService = userService;
}


    @GetMapping("/")
    public String home(Model model) {
        List<Post> posts = postService.getLatestSixPosts();
        model.addAttribute("posts", posts);
        return "index";
    }

    @GetMapping("/new_article")
    public RedirectView processPostForm(HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            String username = (String) session.getAttribute("currentUsername");
            Long newPostId = postService.addNewPost(username);
            // 這裡使用 RedirectView 來重定向到編輯文章的頁面
            return new RedirectView("/article/" + newPostId + "/edit");

        }catch (Postdata_UpdateException | JsonProcessingException e){
            redirectAttributes.addFlashAttribute("errorMessage", "無法創建新文章");
            // 當錯誤發生時重定向回首頁或錯誤頁面
            return new RedirectView("/error");
        }
    }
    @PostMapping("/new_article/image")
    public ResponseEntity<?> handleArticleImageUpload(@RequestParam("image") MultipartFile file, @RequestParam("articleId") Long articleId, HttpSession session){
        if (file != null) {
            if (session.getAttribute("currentUserId") == null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "請先登入"));
            }
            try {
                Map<String, String> imageUrl = postService.saveNewArticleImage(file, articleId);
                return ResponseEntity.ok(imageUrl);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
            }
        }else {
            return ResponseEntity.ok(Map.of("imageUrl", ""));
        }
    }

    @PostMapping("/new_article/image/deleted")
    public ResponseEntity<?> deleteArticleImage(@RequestBody @Valid ImageDto imageDto, HttpSession session){
        Long articleId = imageDto.getArticleId();
        try {
            Post post = postService.getPostByArticle_id(articleId);
            if (session.getAttribute("currentUserId") == null || !(post.getAuthor().getId().equals (session.getAttribute("currentUserId")))){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "請先登入"));
            }
            System.out.println("imageUrl: " + imageDto.getUrl());
            System.out.println("articleId: " + articleId);
            postService.deleteArticleImage(imageDto.getUrl());
            return ResponseEntity.ok(Map.of("message", "圖片已刪除"));
        } catch (Postdata_UpdateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }



    @PutMapping("/new_article/{articleId}")
    public ResponseEntity<?> updatePostContent(@PathVariable Long articleId, @RequestBody @Valid ArticleDto articleDto, HttpSession session) {
        try {
            if (session.getAttribute("currentUserId") == null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "請先登入"));
            }

            postService.updatePostWithContent(articleId, articleDto);
            return ResponseEntity.ok(Map.of("message", "文章保存成功"));
        } catch (Postdata_UpdateException e) {
            String errorMessage = e.getMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", errorMessage));
        } catch (Exception e) {
            // 捕获其他所有的异常
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/article/{articleId}")
    public String articleDetail(@PathVariable Long articleId, Model model, HttpSession session){
        try {
            Long userId = null;
            User user = null;
            Post post = postService.getPostByArticle_id(articleId);
            if (post.isDeleted()){
                return "redirect:/";
            }


            if (!post.isPublished()){

                if (session.getAttribute("currentUserId") != null) {
                    userId = (Long) session.getAttribute("currentUserId");
                }
                if (userId == null || !(userId.equals(post.getAuthor().getId())))
                    return "redirect:/";
            }

            if (session.getAttribute("currentUserId") != null) {
                userId = (Long) session.getAttribute("currentUserId");
                user = userService.getUserById(userId);
            }

            model.addAttribute("formattedCreationTime", formattedTimeForUser(post.getCreationTime(), user));
            model.addAttribute("formattedUpdateTime", formattedTimeForUser(post.getUpdateTime(), user));
            model.addAttribute("post", post);
            return "article_detail";
        } catch (Postdata_UpdateException e) {
            return "redirect:/";
        }
    }

    @GetMapping("/article/{articleId}/content")
    public ResponseEntity<?> getArticleContent(@PathVariable long articleId){
        try {
            Post articleContent = postService.getPostByArticle_id(articleId);
            if (articleContent.isDeleted()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "文章已刪除"));
            }
            Map<String, Object> contentDelta = postService.convertPostStructure(articleContent.getContent(), articleContent);


            return new ResponseEntity<>(contentDelta, HttpStatus.OK);
        } catch (Postdata_UpdateException | JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/article")
    public String listPosts(Model model, @RequestParam(defaultValue = "1") int page, HttpServletRequest request, HttpSession session) {
        int pageSize = 6;
        User user = null;
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("updateTime").descending());
        Page<Post> postPage = postService.getPublishedPostsByPage(pageable);
        model.addAttribute("posts", postPage);
        model.addAttribute("page", page);
        model.addAttribute("totalPages", postPage.getTotalPages());

        if (session.getAttribute("currentUserId") != null){
            Long userId = (Long) session.getAttribute("currentUserId");
            user = userService.getUserById(userId);
        }

        List<String> formattedPostCreationTimes = new ArrayList<>();
        List<String> formattedPostUpdateTimes = new ArrayList<>();
        for (Post post : postPage.getContent()) {
            Date postCreationTime = post.getCreationTime();
            Date postUpdateTime = post.getUpdateTime();
            String formattedPostCreationTime = formattedTimeForUser(postCreationTime, user);
            String formattedPostUpdateTime = formattedTimeForUser(postUpdateTime, user);
            formattedPostCreationTimes.add(formattedPostCreationTime);
            formattedPostUpdateTimes.add(formattedPostUpdateTime);
        }
        model.addAttribute("formattedPostCreationTimes", formattedPostCreationTimes);
        model.addAttribute("formattedPostUpdateTimes", formattedPostUpdateTimes);




        if (postPage.getTotalPages() == 0) {
            if (page != 1) {
                return "redirect:/article?page=1";
            }
        } else {
            if (!request.getParameterMap().containsKey("page") || page > postPage.getTotalPages()) {
                int lastPage = Math.max(postPage.getTotalPages(), 1);
                return "redirect:/article?page=" + lastPage;
            }
        }
        return "article";
    }


    @GetMapping("/article/{articleId}/edit")
    public String editPostForm(@PathVariable Long articleId, Model model, HttpSession session, RedirectAttributes redirectAttributes){
        try {
            String username = (String) session.getAttribute("currentUsername");
            Post post = postService.getPostByArticle_id(articleId);
            if (post.isDeleted()){
                return "redirect:/";
            }
            if (username != null && username.equals(post.getAuthor().getUsername())){
                model.addAttribute("post", post);
                return "edit_article";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "你沒有權限編輯此文章");
                return "redirect:/article/" + articleId;
            }
        }
        catch (Postdata_UpdateException e) {
            return "redirect:/";
        }
    }

    @DeleteMapping("/article/{articleId}/delete")
    public ResponseEntity<?> deletePost(@PathVariable Long articleId, HttpSession session){
        try {
            String username = (String) session.getAttribute("currentUsername");
            if (username != null){
                postService.deletePost(articleId, username);
                return ResponseEntity.ok(Map.of("message", "文章刪除成功"));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "請先登入"));
            }
        } catch (Postdata_UpdateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/article/publish")
    public ResponseEntity<?> publishArticle (HttpSession session, @RequestBody @Valid PublishRequestDto publishRequest) {
        try {
            Long userId = (Long) session.getAttribute("currentUserId");
            if (userId == null || !(postService.getUserByArticle_Id(publishRequest.getId()).getId().equals(userId))) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "請先登入有編輯此文章權限的帳號"));
            }
            postService.updatePostPublish(publishRequest);
            return ResponseEntity.ok(Map.of("message", "文章已更新發布狀態"));
        } catch (Postdata_UpdateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
}
