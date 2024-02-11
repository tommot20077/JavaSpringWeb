package xyz.dowob.blogspring.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import xyz.dowob.blogspring.Exceptions.Userdata_UpdateException;
import xyz.dowob.blogspring.model.User;
import xyz.dowob.blogspring.service.UserService;

@Controller
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register"; // 返回注冊表單視圖
    }


    @PostMapping("/register")
    public String processRegistrationForm(@ModelAttribute User user, RedirectAttributes redirectAttributes,@RequestParam("confirmPassword") String confirmPassword) {
        // 呼叫 service 層來處理用戶注冊
        try {
            userService.registerUser(user ,confirmPassword);
            return "redirect:/register_success"; // 重導到注冊成功頁面
        }catch (Userdata_UpdateException e){
            String errorMessage = switch (e.getErrorCode()) {
                case USERNAME_ALREADY_EXISTS -> "用戶名已經存在!";
                case USERNAME_CONTAINS_ILLEGAL_CHARACTERS -> "用戶名包含非法字符!";
                case PASSWORD_LENGTH_INVALID -> "密碼長度不符合要求!";
                case PASSWORD_CONTAINS_USERNAME -> "密碼不能包含用戶名!";
                case PASSWORD_COMPLEXITY_INSUFFICIENT -> "密碼複雜度不足!";
                case PASSWORD_NOT_MATCH -> "輸入的密碼不一致!";
                case EMAIL_ALREADY_EXISTS -> "Email已經存在!";

            };
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/register";
        }

    }
    @GetMapping("/verify")
    public ModelAndView verifyEmail(@RequestParam String token) {
        ModelAndView modelAndView = new ModelAndView("verify"); // verify.html模板的名称

        try {
            if (userService.verifyToken(token)) {
                modelAndView.addObject("message", "驗證成功"); // 待显示的成功消息
                modelAndView.addObject("verified", true);
            } else {
                modelAndView.addObject("message", "驗證失敗：無效的token或token已過期"); // 待显示的失败消息
                modelAndView.addObject("verified", false);
            }
        } catch (UsernameNotFoundException e) {
            modelAndView.addObject("message", e.getMessage());
            modelAndView.addObject("verified", false);
        } catch (Exception e) {
            modelAndView.addObject("message", "驗證過程中出現了問題：" + e.getMessage());
            modelAndView.addObject("verified", false);
        }

        return modelAndView;
    }

    @GetMapping("/register_success")
    public String showRegistrationSuccess() {
        return "register_success";
    }


    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("user", new User());
        return "login";
    }

    @GetMapping("/login_success")
    public String showLogin_success() {return "login_success";}

    @PostMapping("/login")
    public String performLogin(@ModelAttribute User user, HttpSession session, RedirectAttributes redirectAttributes){
        if(userService.authenticate(user.getUsername(), user.getPassword())){
            session.setAttribute("currentUsername", user.getUsername());
            session.setAttribute("currentUserId", user.getId());
            return "redirect:/login_success";
        }
        else{
            redirectAttributes.addFlashAttribute("errorMessage","帳號或密碼錯誤");
            return "redirect:/login";
        }
    }


    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/profile")
    public String showProfileForm(Model model, HttpSession session) {
        try {
            String username = (String) session.getAttribute("currentUsername");
            User user = userService.getUserByUsername(username);
            model.addAttribute("user", user);
            return "profile";
        } catch (UsernameNotFoundException e) {
            return "redirect:/login";
        }

    }

    @PostMapping("/profile")
    public String processProfileForm(@ModelAttribute User user, HttpSession session, RedirectAttributes redirectAttributes, @RequestParam("confirmPassword") String confirmPassword){
        String username = (String) session.getAttribute("currentUsername");
        User repositoryUser = userService.getUserByUsername(username);
        try {
            userService.updateUser(user, repositoryUser, confirmPassword);
            redirectAttributes.addFlashAttribute("success", "更新成功");

        } catch (Userdata_UpdateException e) {
            String errorMessage = switch (e.getErrorCode()) {
                case PASSWORD_LENGTH_INVALID -> "密碼長度不符合要求!";
                case PASSWORD_CONTAINS_USERNAME -> "密碼不能包含用戶名!";
                case PASSWORD_COMPLEXITY_INSUFFICIENT -> "密碼複雜度不足!";
                case PASSWORD_NOT_MATCH -> "輸入的密碼不一致!";
                case EMAIL_ALREADY_EXISTS -> "Email已經存在!";
                default -> "未知錯誤";
            };
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/profile";
        }
        return "redirect:/profile";
    }
}
