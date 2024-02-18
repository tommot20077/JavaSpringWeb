package xyz.dowob.blogspring.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.dowob.blogspring.functions.CommentDto;
import xyz.dowob.blogspring.model.Comment;
import xyz.dowob.blogspring.service.CommentService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class CommentController {
    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/article/{articleId}/comment")
    public ResponseEntity<?> postComment(@PathVariable long articleId, @RequestBody @Valid CommentDto commentDto, HttpSession session){
        String commentUsername = (String) session.getAttribute("currentUsername");
        if(commentUsername == null || commentUsername.trim().isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "請先登入"));
        }

        boolean result = commentService.saveComment(commentDto.getDelta(), articleId, commentUsername);

        if(result){
            return ResponseEntity.ok(Map.of("message", "評論保存成功"));
        }else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "評論不可僅有空白或空白字符"));
        }
    }

    @GetMapping("/article/{articleId}/comment")
    public ResponseEntity<?> getComments(@PathVariable long articleId){
        List<Comment> comments = commentService.getCommentsByArticleId(articleId);
        List<Map<String, Object>> commentDelta = comments.stream().map(comment -> {
            try {
                return commentService.convertCommentStructure(comment.getContent());
            } catch (JsonProcessingException e) {
                throw new RuntimeException("轉換JSON失敗", e);
            }
        }).collect(Collectors.toList());
        return new ResponseEntity<>(commentDelta, HttpStatus.OK);
    }

    @PostMapping("/article/{articleId}/comment/image")
    public ResponseEntity<?> handleImageUpload(@PathVariable long articleId, @RequestParam("image")MultipartFile file, HttpSession session){
        String commentUsername = (String) session.getAttribute("currentUsername");
        if(commentUsername == null || commentUsername.trim().isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "請先登入"));
        }
        try {
            String imageUrl = commentService.saveCommentImage(file, articleId);

            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

}
