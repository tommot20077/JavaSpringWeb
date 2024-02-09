package xyz.dowob.blogspring.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import xyz.dowob.blogspring.Exceptions.Postdata_UpdateException;
import xyz.dowob.blogspring.model.Post;
import xyz.dowob.blogspring.model.User;
import xyz.dowob.blogspring.repository.UserRepository;
import xyz.dowob.blogspring.service.PostService;

import java.util.List;

@Controller


public class PostController {
    @Autowired
    private final PostService postService;
    private final UserRepository userRepository;
//userRepository處理
    public PostController(PostService postService,UserRepository userRepository) {
        this.postService = postService;
        this.userRepository = userRepository;
    }


    @GetMapping("/")
    public String home(Model model) {
        List<Post> posts = postService.findAllPosts(); // 假定你的服務層有這個方法
        model.addAttribute("posts", posts);
        return "index"; // 假定你的主頁模板名為index.html
    }

    @GetMapping("/new_article")
    public String newPostForm(Model model){
        model.addAttribute("post", new Post());
        return "new_article";
    }

    @PostMapping("/new_article")
    public String processPostForm(@ModelAttribute Post post, RedirectAttributes redirectAttributes, HttpSession session) {
        try {
            String username = (String) session.getAttribute("currentUsername");
            if (username == null || username.trim().isEmpty()) throw new Postdata_UpdateException(Postdata_UpdateException.ErrorCode.DID_NOT_LOGIN);
            else {
                User author = userRepository.findByUsername(username).orElseThrow();
                postService.addNewPost(post, author);
                return "redirect:/new_article_success";
            }
        }catch (Postdata_UpdateException e){
            String errorMessage = switch (e.getErrorCode()) {
                case DID_NOT_LOGIN -> "請先登入!";
                case POSTDATA_UPDATE_FAILED -> "文章發布失敗!";
                case CONTENT_TOO_LONG -> "內容太長!";
                case TITLE_TOO_LONG -> "標題太長!";
                default -> "未知錯誤!";

            };
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/new_article";
        }
    }

    @GetMapping("/new_article_success")
    public String newPostSuccess(){
        return "new_article_success";
    }


    @GetMapping("/article/{id}")
    public String articleDetail(@PathVariable Long id, Model model){
        try {Post post = postService.findPostByArticle_id(id);
            model.addAttribute("post", post);
            return "article_detail";
        } catch (Postdata_UpdateException e) {
            return "redirect:/";
        }
    }

    @GetMapping("/article")
    public String listPosts(Model model, @RequestParam(defaultValue = "1") int page, HttpServletRequest request){
        int pageSize = 5;
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Post> postPage = postService.allPostsByPage(pageable);
        model.addAttribute("posts", postPage.getContent());
        model.addAttribute("pageNum", page);
        model.addAttribute("totalPages", postPage.getTotalPages());
        if (request.getParameterMap().containsKey("page") && page < 1) {
            return "redirect:/article?page=1";
        } else if (request.getParameterMap().containsKey("page") && page > postPage.getTotalPages()) {
            return "redirect:/article?page=" + postPage.getTotalPages();
        } else if (!request.getParameterMap().containsKey("page")) {
            return "redirect:/article?page=1";
        }
        return "article";
    }


}
