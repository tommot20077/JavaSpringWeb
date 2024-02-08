package xyz.dowob.blogspring.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import xyz.dowob.blogspring.UserException.RegisterException;
import xyz.dowob.blogspring.functions.UserInspection;
import xyz.dowob.blogspring.model.User;
import xyz.dowob.blogspring.repository.UserRepository;
import xyz.dowob.blogspring.service.UserService;

@Controller
public class UserController {
    //private final UserRepository userRepository;
    private final UserService userService;
    //private final UserInspection userInspection;

    @Autowired
    public UserController(UserInspection userInspection,UserRepository userRepository, UserService userService) {
        //this.userRepository = userRepository;
        this.userService = userService;
        //this.userInspection = userInspection;
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
            redirectAttributes.addFlashAttribute("success", "注冊成功!");
            return "redirect:/register_success"; // 重導到注冊成功頁面
        }catch (RegisterException e){
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
            redirectAttributes.addFlashAttribute("success","登入成功");
            session.setAttribute("currentUser", user.getUsername());
            return "redirect:/login_success";
        }
        else{
            redirectAttributes.addFlashAttribute("errorMessage","帳號或密碼錯誤");
            return "redirect:/login";
        }
    }


    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success","登出成功");
        return "redirect:/";
    }

    @GetMapping("/profile")
    public String showProfileForm(Model model, HttpSession session) {
        try {
            String username = (String) session.getAttribute("currentUser");
            User user = userService.getUserByUsername(username);
            model.addAttribute("user", user);
            return "profile";
        } catch (UsernameNotFoundException e) {
            return "redirect:/login";
        }

    }

    @PostMapping("/profile")
    public String processProfileForm(@ModelAttribute User user, HttpSession session, RedirectAttributes redirectAttributes, @RequestParam("confirmPassword") String confirmPassword){
        String username = (String) session.getAttribute("currentUser");
        User repositoryUser = userService.getUserByUsername(username);
        try {
            userService.updateUser(user, repositoryUser, confirmPassword);
            redirectAttributes.addFlashAttribute("success", "更新成功");

        } catch (RegisterException e) {
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
