package xyz.dowob.blogspring.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import xyz.dowob.blogspring.Exceptions.Postdata_UpdateException;
import xyz.dowob.blogspring.functions.ArticleDto;
import xyz.dowob.blogspring.model.Post;
import xyz.dowob.blogspring.service.PostService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller


public class PostController {

    private final PostService postService;


//userRepository處理
@Autowired
    public PostController(PostService postService) {
        this.postService = postService;

    }


    @GetMapping("/")
    public String home(Model model) {
        List<Post> posts = postService.getAllPosts();
        model.addAttribute("posts", posts);
        return "index";
    }

    @GetMapping("/new_article")
    public String newPostForm(Model model){
        model.addAttribute("post", new Post());
        return "new_article";
    }

    @PostMapping("/new_article")
    public ResponseEntity<?> processPostForm(HttpSession session) {
        try {
            String username = (String) session.getAttribute("currentUsername");
            if (session.getAttribute("currentUserId") == null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "請先登入"));
            }


            Long newPostId = postService.addNewPost(username);

            return ResponseEntity.ok(Map.of("message", "文章建立成功", "articleId", newPostId));

        }catch (Postdata_UpdateException |JsonProcessingException e){
            String errorMessage = e.getMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", errorMessage));
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
                System.out.println("imageUrl: " + imageUrl);
                return ResponseEntity.ok(imageUrl);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
            }
        }else {
            return ResponseEntity.ok(Map.of("imageUrl", ""));
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




    @GetMapping("/new_article_success")
    public String newPostSuccess(){
        return "new_article_success";
    }


    @GetMapping("/article/{articleId}")
    public String articleDetail(@PathVariable Long articleId, Model model){
        try {
            Post post = postService.getPostByArticle_id(articleId);
            if (post.isDeleted()){
                return "redirect:/";
            }
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
    public String listPosts(Model model, @RequestParam(defaultValue = "1") int page, HttpServletRequest request){
        int pageSize = 6;
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Post> postPage = postService.getAllPostsByPage(pageable);
        model.addAttribute("posts", postPage);
        model.addAttribute("page", page);
        model.addAttribute("totalPages", postPage.getTotalPages());


        if (!request.getParameterMap().containsKey("page") && (request.getParameterMap().containsKey("page") && page < 1)) {
            return "redirect:/article?page=1";
        } else if (request.getParameterMap().containsKey("page") && page > postPage.getTotalPages()) {
            return "redirect:/article?page=" + postPage.getTotalPages();
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
}
