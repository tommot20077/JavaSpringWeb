package xyz.dowob.blogspring.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import xyz.dowob.blogspring.model.Post;
import xyz.dowob.blogspring.model.User;
import xyz.dowob.blogspring.repository.PostRepository;
import xyz.dowob.blogspring.repository.UserRepository;

@Controller
public class SearchController {
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public SearchController(UserRepository userRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    // 修改GetMapping以包含defaultValue，並使keyword與type參數選填
    @GetMapping("/search")
    public String search(
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(value = "type", required = false, defaultValue = "posts") String type,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            Model model) {

        Pageable pageable = PageRequest.of(page - 1 , 10);
        if (type.equals("users")) {
            Page<User> users = userRepository.searchByUsernameOrEmail(keyword, pageable);
            model.addAttribute("usersPage", users);
            model.addAttribute("totalPages", users.getTotalPages());
        } else {
            Page<Post> posts = postRepository.searchByTitle(keyword, pageable);
            model.addAttribute("postsPage", posts);
            model.addAttribute("totalPages", posts.getTotalPages());
        }
        model.addAttribute("keyword", keyword);
        model.addAttribute("type", type);
        model.addAttribute("page", page);

        return "search";
    }
}