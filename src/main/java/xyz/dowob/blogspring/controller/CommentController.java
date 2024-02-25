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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.dowob.blogspring.functions.CommentDto;
import xyz.dowob.blogspring.model.Comment;
import xyz.dowob.blogspring.service.CommentService;

import java.io.IOException;
import java.util.HashMap;
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "請先登入"));
        }

        boolean result = commentService.saveComment(commentDto.getDelta(), articleId, commentUsername);
        if(result){
            return ResponseEntity.ok(Map.of("message", "評論保存成功"));
        }else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "評論不可僅有空白或空白字符"));
        }
    }

    @GetMapping("/article/{articleId}/comment")
    public ResponseEntity<?> getComments(@PathVariable long articleId ,@RequestParam(defaultValue = "0") int page ){

        Pageable pageable = PageRequest.of(page, 10);
        Page<Comment> commentPage = commentService.getCommentsByArticleId(articleId, pageable);

        List<Map<String, Object>> commentDeltaList = commentPage.getContent().stream().map(comment -> {
            Map<String, Object> commentData = new HashMap<>();
            try {
                Map<String, Object> commentDelta = commentService.convertCommentStructure(comment.getContent());
                commentData.put("delta", commentDelta);
                commentData.put("commentInArticleId", comment.getCommentInArticleId());
                commentData.put("author", comment.getAuthor().getUsername());
                commentData.put("creation_time", comment.getCreation_time());
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
    public ResponseEntity<?> handleCommentImageUpload(@PathVariable long articleId, @RequestParam("image")MultipartFile file, HttpSession session){
        String commentUsername = (String) session.getAttribute("currentUsername");
        if(commentUsername == null || commentUsername.trim().isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "請先登入"));
        }
        try {
            String imageUrl = commentService.saveCommentImage(file, articleId);

            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

}
