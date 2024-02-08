package xyz.dowob.blogspring.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import xyz.dowob.blogspring.Exceptions.Postdata_UpdateException;
import xyz.dowob.blogspring.model.Post;
import xyz.dowob.blogspring.model.User;
import xyz.dowob.blogspring.repository.UserRepository;
import xyz.dowob.blogspring.service.PostService;

@Controller


public class PostController {
    @Autowired
    private final PostService postService;
    private final UserRepository userRepository;

    public PostController(PostService postService,UserRepository userRepository) {
        this.postService = postService;
        this.userRepository = userRepository;
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
                return "redirect:/";
            }
        }catch (Postdata_UpdateException e){
            String errorMessage = switch (e.getErrorCode()) {
                case DID_NOT_LOGIN -> "請先登入!";
                case POSTDATA_UPDATE_FAILED -> "文章發布失敗!";
                case CONTENT_TOO_LONG -> "內容太長!";
                case TITLE_TOO_LONG -> "標題太長!";

            };
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/new_article";
        }


    }


}
