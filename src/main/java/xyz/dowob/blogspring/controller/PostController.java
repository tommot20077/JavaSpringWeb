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
import xyz.dowob.blogspring.service.UserService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller


public class PostController {

    private final PostService postService;
    private final UserService userService;

//userRepository處理
@Autowired
    public PostController(PostService postService, UserService userService) {
        this.postService = postService;
        this.userService = userService;

    }


    @GetMapping("/")
    public String home(Model model) {
        List<Post> posts = postService.getAllPosts(); // 假定你的服務層有這個方法
        model.addAttribute("posts", posts);
        return "index"; // 假定你的主頁模板名為index.html
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
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","請先登入"));
            }


            Long newPostId = postService.addNewPost(username);

            return ResponseEntity.ok(Map.of("message", "評論保存成功", "articleId", newPostId));

        }catch (Postdata_UpdateException |JsonProcessingException e){
            String errorMessage = e.getMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", errorMessage));
        }
    }
    @PostMapping("/new_article/image")
    public ResponseEntity<?> handleArticleImageUpload(@RequestParam("image") MultipartFile file, @RequestParam("articleId") Long articleId, HttpSession session){
        if (file != null) {
            String commentUsername = (String) session.getAttribute("currentUsername");
            if(commentUsername == null || commentUsername.trim().isEmpty()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "請先登入"));
            }
            try {
                String imageUrl = postService.saveNewArticleImage(file, articleId);
                return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
            }
        }else {
            return ResponseEntity.ok(Map.of("imageUrl", ""));
        }
    }

    @PutMapping("/new_article/{articleId}")
    public ResponseEntity<?> updatePostWithContent(@PathVariable Long articleId, @RequestBody @Valid ArticleDto articleDto, HttpSession session) {
        try {
            String authorUsername = (String) session.getAttribute("currentUsername");
            if (authorUsername == null || authorUsername.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "請先登入"));
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
            System.out.println("回傳內容原:" +articleContent.getContent());
            Map<String, Object> contentDelta = postService.convertPostStructure(articleContent.getContent());
            System.out.println("回傳內容轉:" + contentDelta);


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

  /*  @PostMapping("/article/{articleId}/edit")
    public String processEditPostForm(@PathVariable Long articleId, @ModelAttribute Post updatePost, RedirectAttributes redirectAttributes){
        try {
            Post OriginPost = postService.getPostByArticle_id(articleId);
            postService.updatePost(updatePost, OriginPost);
            redirectAttributes.addFlashAttribute("success", "文章編輯成功");
            return "redirect:/article/" + articleId ;
        } catch (Postdata_UpdateException e) {
            String errorMessage = e.getMessage();
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/article/" + articleId + "/edit";
        }
    }
*/




}
