package xyz.dowob.blogspring.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import xyz.dowob.blogspring.Exceptions.Userdata_UpdateException;
import xyz.dowob.blogspring.config.UserConfig;
import xyz.dowob.blogspring.functions.RcloneExecutor;
import xyz.dowob.blogspring.model.Post;
import xyz.dowob.blogspring.model.User;
import xyz.dowob.blogspring.service.ApiTokenService;
import xyz.dowob.blogspring.service.PostService;
import xyz.dowob.blogspring.service.TokenService;
import xyz.dowob.blogspring.service.UserService;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static xyz.dowob.blogspring.functions.FormattedTimeForUser.formattedTimeForUser;

@Controller
public class UserController {

    private final UserService userService;
    private final TokenService tokenService;
    private final PostService postService;
    private final ApiTokenService apiTokenService;

    @Autowired
    public UserController(UserService userService, TokenService tokenService, PostService postService, ApiTokenService apiTokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.postService = postService;
        this.apiTokenService = apiTokenService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        return "register";
    }


    @PostMapping("/register")
    public String processRegistrationForm(@ModelAttribute("user") @Valid User user, BindingResult result, RedirectAttributes redirectAttributes, @NotNull @RequestParam("confirmPassword") String confirmPassword) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.user", result);
            redirectAttributes.addFlashAttribute("user", user);
            return "redirect:/register";
        }

        try {
            userService.registerUser(user ,confirmPassword);
            return "redirect:/register_success";
        }catch (Exception e){
            String errorMessage = e.getMessage();
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            redirectAttributes.addFlashAttribute("user", user);
            return "redirect:/register";
        }

    }

    @GetMapping("/register_success")
    public String showRegistrationSuccess() {
        return "register_success";
    }

    @GetMapping("/verify")
    public ModelAndView verifyEmail(@RequestParam(defaultValue = "") String token) {
        ModelAndView modelAndView = new ModelAndView("verify");
        try {
            if (StringUtils.hasText(token) && tokenService.verifyActiveEmailToken(token)) {
                modelAndView.addObject("message", "驗證成功");
                modelAndView.addObject("verified", true);
            } else {
                modelAndView.addObject("message", "驗證失敗：無效的token或token已過期");
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
    public String performLogin(@ModelAttribute User user,
                               @RequestParam(value = "rememberMe", defaultValue = "false") boolean rememberMe,
                               HttpSession session,
                               RedirectAttributes redirectAttributes,
                               HttpServletResponse response){

        if(userService.authenticate(user.getUsername(), user.getPassword())){
            User updatedUser = userService.getUserByUsername(user.getUsername());
            if (rememberMe) {
                userService.createRememberMeCookie(response, updatedUser.getId());
            }
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
    public String logout(HttpServletRequest request, HttpSession session, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        userService.deleteRememberMeCookie(response, session, cookies);
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/profile")
    public String showProfileForm(Model model, HttpSession session) {
        try {
            List<String> timezones = ZoneId.getAvailableZoneIds()
                                    .stream()
                                    .sorted()
                                    .collect(Collectors.toList());
            model.addAttribute("timezones", timezones);

            Long userid = (Long) session.getAttribute("currentUserId");
            if (userid == null) {
                return "redirect:/login";
            }
            User user = userService.getUserById(userid);
            model.addAttribute("user", user);
            return "profile";
        } catch (UsernameNotFoundException e) {
            return "redirect:/login";
        }

    }


    @PostMapping("/basicProfile")
    public String processBasicProfileForm(@ModelAttribute User newInputUser,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes,
                                     HttpServletResponse response,
                                     HttpServletRequest request
    ){
        try {
            long userid = (Long) session.getAttribute("currentUserId");
            User repositoryUser = userService.getUserById(userid);
            userService.updateBasicUser(newInputUser, repositoryUser);
            redirectAttributes.addFlashAttribute("success", "更新成功!");

            Cookie[] cookies = request.getCookies();
            userService.deleteRememberMeCookie(response, session, cookies);
            return "redirect:/profile";

        } catch (UsernameNotFoundException e) {
            String errorMessage = e.getMessage();
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/profile";
        }
    }


    @PostMapping("/importantProfile")
    public String processImportantProfileForm(@ModelAttribute User newInputUser,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes,
                                     @RequestParam("confirmPassword") String confirmPassword,
                                     @RequestParam("originPassword") String originPassword,
                                     HttpServletResponse response,
                                     HttpServletRequest request
                                     ){
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

            Cookie[] cookies = request.getCookies();
            userService.deleteRememberMeCookie(response, session, cookies);
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
    public ResponseEntity<?> sendResetPasswordMail(@RequestBody Map<String, String> keyword, HttpSession session){
        String username_Or_Email = keyword.get("usernameOrEmail");
        try {
            User user = userService.getUserByUsernameOrEmail(username_Or_Email);
            userService.sendResetPasswordMail(user);
            session.setAttribute("resetPasswordUserID", user.getId());
            return ResponseEntity.ok("驗證信已發送");
        } catch (Userdata_UpdateException | UsernameNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }

    }

    @PostMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> payload, HttpSession session, HttpServletRequest request, HttpServletResponse response){
        long userID = Long.parseLong((session.getAttribute("resetPasswordUserID").toString()));
        User user = userService.getUserById(userID);
        String verificationCode = payload.get("verificationCode");
        String newPassword = payload.get("newPassword");
        String reNewPassword = payload.get("reNewPassword");
        try {
            userService.resetPassword(user, verificationCode, newPassword, reNewPassword);
            Cookie[] cookies = request.getCookies();
            userService.deleteRememberMeCookie(response, session, cookies);
            return ResponseEntity.ok("重設密碼成功");
        } catch (Userdata_UpdateException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }


    @GetMapping("/user/{id}")
    public String showUserDetail(@PathVariable Long id, Model model, @RequestParam(defaultValue = "1") int page, HttpServletRequest request, HttpSession session){
        try {
            long userid = 0;
            int pageSize = 6;
            Page<Post> posts;
            User user = userService.getUserById(id);
            Pageable pageable = PageRequest.of(page -1, pageSize, Sort.by("updateTime").descending());
            if (session.getAttribute("currentUserId") != null){
                userid = (Long) session.getAttribute("currentUserId");
            }

            if (id == userid) {
                posts = postService.getPostsByAuthorId(id, pageable, false);
            } else {
                posts = postService.getPostsByAuthorId(id, pageable, true);
            }
            model.addAttribute("user", user);
            model.addAttribute("postPage", posts);
            model.addAttribute("pageNum", page);
            model.addAttribute("totalPages", posts.getTotalPages());
            model.addAttribute("formattedUserTime", formattedTimeForUser(user.getRegister_time(), user));


            List<String> formattedPostCreationTimes = new ArrayList<>();
            List<String> formattedPostUpdateTimes = new ArrayList<>();
            for (Post post : posts.getContent()) {
                Date postCreationTime = post.getCreationTime();
                Date postUpdateTime = post.getUpdateTime();
                String formattedPostCreationTime = formattedTimeForUser(postCreationTime, user);
                String formattedPostUpdateTime = formattedTimeForUser(postUpdateTime, user);
                formattedPostCreationTimes.add(formattedPostCreationTime);
                formattedPostUpdateTimes.add(formattedPostUpdateTime);
            }
            model.addAttribute("formattedPostCreationTimes", formattedPostCreationTimes);
            model.addAttribute("formattedPostUpdateTimes", formattedPostUpdateTimes);


            if (!request.getParameterMap().containsKey("page") && (request.getParameterMap().containsKey("page") && page < 1)) {
                return "redirect:/user/{id}?page=1";
            } else if (request.getParameterMap().containsKey("page") && page > posts.getTotalPages()) {
                return "redirect:/user/{id}?page=" + posts.getTotalPages();
            }
            return "user_detail";
        } catch (UsernameNotFoundException e) {
            return "redirect:/";
        }
    }
    @ResponseBody
    @GetMapping("/weather")
    public ResponseEntity<String> getWeather(HttpServletRequest request) {
        Logger logger = LoggerFactory.getLogger(RcloneExecutor.class);
        String clientIp = userService.getClientIp(request);
        boolean canFetchData = apiTokenService.incrementTokenAndCheckLimit(clientIp);
        if (!canFetchData) {
            logger.error("超過每小時請求限制");
            return ResponseEntity.status(429).body("請求過於頻繁，請稍後再試");
        }

        RestTemplate restTemplate = new RestTemplate();
        UserConfig userConfig = UserConfig.standardSetupCommand("config.properties");
        String url = userConfig.getWeatherApiUrl();
        String apiKey = userConfig.getWeatherApiKey();

        String postUrl = url.replace("{inquire}", clientIp).replace("{apikey}", apiKey);

        try {
            String weatherResponse = restTemplate.getForObject(postUrl, String.class);
            logger.info("成功獲取天氣資料");
            return ResponseEntity.ok(weatherResponse);
        } catch (Exception e) {
            logger.error("無法獲取天氣資料: {}", e.getMessage());
            return ResponseEntity.status(500).body("請求錯誤，請稍後再試");
        }
    }
}
