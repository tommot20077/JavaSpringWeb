package xyz.dowob.blogspring.controller;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import xyz.dowob.blogspring.UserException.RegisterException;
import xyz.dowob.blogspring.model.User;
import xyz.dowob.blogspring.repository.UserRepository;
import xyz.dowob.blogspring.service.UserService;

@Controller
public class UserController {
    private final UserRepository userRepository;
    private final UserService userService;
    @Autowired
    public UserController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
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
            if(!user.getPassword().equals(confirmPassword)) throw new RegisterException(RegisterException.ErrorCode.PASSWORD_NOT_MATCH);
            userService.registerUser(user);
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

    @PostMapping("/login")
        public String performLogin(@ModelAttribute User user, HttpServletRequest request, RedirectAttributes redirectAttributes){
            if(userService.authenticate(user.getUsername(), user.getPassword())){
                redirectAttributes.addFlashAttribute("success","登入成功");
                return "redirect:/";
            }
            else{
                redirectAttributes.addFlashAttribute("errorMessage","帳號或密碼錯誤");
                return "redirect:/login";
            }
        }


}
