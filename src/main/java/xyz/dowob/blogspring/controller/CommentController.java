package xyz.dowob.blogspring.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import xyz.dowob.blogspring.Exceptions.Commentdata_UpdateException;
import xyz.dowob.blogspring.functions.CommentDto;
import xyz.dowob.blogspring.model.Comment;
import xyz.dowob.blogspring.model.User;
import xyz.dowob.blogspring.service.CommentService;
import xyz.dowob.blogspring.service.UserService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static xyz.dowob.blogspring.functions.FormattedTimeForUser.formattedTimeForUser;

@Controller
public class CommentController {
    private final CommentService commentService;

    private final UserService userService;

    @Autowired
    public CommentController(CommentService commentService, UserService userService) {
        this.commentService = commentService;
        this.userService = userService;
    }

    @PostMapping("/article/{articleId}/comment")
    public ResponseEntity<?> postComment(@PathVariable long articleId, @RequestBody @Valid CommentDto commentDto, HttpSession session) {
        String commentUsername = (String) session.getAttribute("currentUsername");
        if (session.getAttribute("currentUserId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "請先登入"));
        }

        boolean result = commentService.saveComment(commentDto.getDelta(), articleId, commentUsername);
        if (result) {
            return ResponseEntity.ok(Map.of("message", "評論保存成功"));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "評論不可僅有空白或空白字符"));
        }
    }

    @GetMapping("/article/{articleId}/comment")
    public ResponseEntity<?> getComments(@PathVariable long articleId, @RequestParam(defaultValue = "0") int page, HttpSession session) {
        User user;
        if (session.getAttribute("currentUserId") != null) {
            user = userService.getUserById((Long) session.getAttribute("currentUserId"));
        } else {
            user = null;
        }
        Pageable pageable = PageRequest.of(page, 10);
        Page<Comment> commentPage = commentService.getCommentsByArticleId(articleId, pageable);

        List<Map<String, Object>> commentDeltaList = commentPage.getContent().stream().map(comment -> {
            Map<String, Object> commentData = new HashMap<>();
            try {
                Map<String, Object> commentDelta = commentService.convertCommentStructure(comment.getContent(), comment);
                commentData.put("delta", commentDelta);
                commentData.put("commentInArticleId", comment.getCommentInArticleId());
                commentData.put("commentId", comment.getCommentId());
                commentData.put("authorName", comment.getAuthor().getUsername());
                commentData.put("authorId", comment.getAuthor().getId());
                commentData.put("update_time", formattedTimeForUser(comment.getUpdate_time(), user));
                commentData.put("isDeleted", comment.isDeleted());
                return commentData;
            } catch (JsonProcessingException e) {
                throw new RuntimeException("轉換JSON失敗", e);
            }
        }).collect(Collectors.toList());

        Map<String, Object> paginatedResponse = new HashMap<>();
        paginatedResponse.put("comments", commentDeltaList);
        paginatedResponse.put("currentPage", commentPage.getNumber());
        paginatedResponse.put("totalPages", commentPage.getTotalPages());

        return new ResponseEntity<>(paginatedResponse, HttpStatus.OK);
    }

    @PostMapping("/article/{articleId}/comment/image")
    public ResponseEntity<?> handleCommentImageUpload(@PathVariable long articleId, @RequestParam("image") MultipartFile file, HttpSession session) {
        if (session.getAttribute("currentUserId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "請先登入"));
        }
        try {
            String imageUrl = commentService.saveCommentImage(file, articleId);

            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/comment/edit/{commentId}")
    public String editCommentForm(@PathVariable Long commentId, Model model, HttpSession session) {
        try {
            String currentUserId = Long.toString((Long) session.getAttribute("currentUserId"));
            if (commentService.getCommentByCommentId(commentId).isDeleted() || !commentService.isUserEditable(commentId, currentUserId)) {
                return "redirect:/";
            }
            Comment comment = commentService.getCommentByCommentId(commentId);
            model.addAttribute("comment", comment);
            return "edit_comment";
        } catch (Commentdata_UpdateException e) {
            return "redirect:/";
        }
    }

    @GetMapping("/comment/{commentId}/content")
    public ResponseEntity<?> getCommentContent(@PathVariable long commentId) {
        try {
            Comment comment = commentService.getCommentByCommentId(commentId);
            Map<String, Object> contentDelta = commentService.convertCommentStructure(comment.getContent(), comment);
            return ResponseEntity.ok(Map.of("delta", contentDelta));
        } catch (Commentdata_UpdateException | JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }


    @PutMapping("/comment/edit/{commentId}")
    public ResponseEntity<?> editComment(@PathVariable long commentId, @RequestBody @Valid CommentDto commentDto, HttpSession session) {
        try {
            if (session.getAttribute("currentUserId") == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "請先登入"));
            }
            String currentUserId = Long.toString((Long) session.getAttribute("currentUserId"));
            commentService.editComment(commentDto.getDelta(), commentId, currentUserId);
            return ResponseEntity.ok(Map.of("message", "評論修改成功"));

        } catch (Commentdata_UpdateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }

    }

    @DeleteMapping("/comment/delete/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable long commentId, HttpSession session) {
        try {
            if (session.getAttribute("currentUserId") == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "請先登入"));
            }
            String currentUserId = Long.toString((Long) session.getAttribute("currentUserId"));
            commentService.deleteComment(commentId, currentUserId);
            return ResponseEntity.ok(Map.of("message", "評論刪除成功"));
        } catch (Commentdata_UpdateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }

}
