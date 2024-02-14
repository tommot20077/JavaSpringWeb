package xyz.dowob.blogspring.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import xyz.dowob.blogspring.Exceptions.Userdata_UpdateException;
import xyz.dowob.blogspring.model.User;
import xyz.dowob.blogspring.service.TokenService;
import xyz.dowob.blogspring.service.UserService;

import java.util.Map;

@Controller
public class UserController {

    private final UserService userService;
    private final TokenService tokenService;

    @Autowired
    public UserController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
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
            String errorMessage = e.getMessage();
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/register";
        }

    }

    @GetMapping("/register_success")
    public String showRegistrationSuccess() {
        return "register_success";
    }

    @GetMapping("/verify")
    public ModelAndView verifyEmail(@RequestParam String token) {
        ModelAndView modelAndView = new ModelAndView("verify"); // verify.html模板的名称

        try {
            if (tokenService.verifyActiveEmailToken(token)) {
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
            User updatedUser = userService.getUserByUsername(user.getUsername());
            session.setAttribute("currentUsername", updatedUser.getUsername());
            session.setAttribute("currentUserId", updatedUser.getId());
            session.setAttribute("currentUserEmailStatus", updatedUser.getEmailActiveStatus());
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
            long userid = (Long) session.getAttribute("currentUserId");
            User user = userService.getUserById(userid);
            model.addAttribute("user", user);
            return "profile";
        } catch (UsernameNotFoundException e) {
            return "redirect:/login";
        }

    }

    @PostMapping("/profile")
    public String processProfileForm(@ModelAttribute User newInputUser, HttpSession session, RedirectAttributes redirectAttributes, @RequestParam("confirmPassword") String confirmPassword, @RequestParam("originPassword") String originPassword){
        try {
            long userid = (Long) session.getAttribute("currentUserId");
            User repositoryUser = userService.getUserById(userid);
            String originEmail = repositoryUser.getEmail();
            userService.updateUser(newInputUser, repositoryUser, confirmPassword, originPassword);
            if(!newInputUser.getEmail().equals(originEmail)){
                redirectAttributes.addFlashAttribute("success", "更新成功!\n請至信箱驗證新的電子郵件");
            }else{
                redirectAttributes.addFlashAttribute("success", "更新成功!");
            }
            return "redirect:/profile";

        } catch (Userdata_UpdateException | UsernameNotFoundException e) {
            String errorMessage = e.getMessage();
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/profile";
        }

    }



    @GetMapping("/reset_password")
    public String showResetPasswordForm() {
        return "reset_password";
    }

    @PostMapping("/sendResetPasswordMail")
    public ResponseEntity<?> sendResetPasswordMail(@RequestBody Map<String, String> payload, HttpSession session){
        String username_Or_Email = payload.get("usernameOrEmail");
        try {
            User user = userService.getUserByUsernameOrEmail(username_Or_Email);
            userService.sendResetPasswordMail(user);
            session.setAttribute("resetPasswordUserID", user);
            return ResponseEntity.ok("驗證信已發送");
        } catch (Userdata_UpdateException | UsernameNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }

    }

    @PostMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> payload, HttpSession session){
        long userID = Long.parseLong((session.getAttribute("resetPasswordUserID").toString()));
        User user = userService.getUserById(userID);
        String verificationCode = payload.get("verificationCode");
        String newPassword = payload.get("newPassword");
        String reNewPassword = payload.get("reNewPassword");
        try {
            userService.resetPassword(user, verificationCode, newPassword, reNewPassword);
            return ResponseEntity.ok("重設密碼成功");
        } catch (Userdata_UpdateException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
}
