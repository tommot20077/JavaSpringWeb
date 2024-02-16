package xyz.dowob.blogspring.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.dowob.blogspring.Exceptions.Postdata_UpdateException;
import xyz.dowob.blogspring.functions.DeltaToJsonConverter;
import xyz.dowob.blogspring.model.Comment;
import xyz.dowob.blogspring.repository.CommentRepository;

import java.util.List;
import java.util.Map;

@Service
public class CommentService {

    private CommentRepository commentRepository;
    private PostService postService;
    private UserService userService;
    private final DeltaToJsonConverter deltaToJsonConverter = new DeltaToJsonConverter();
    @Autowired
    public CommentService(CommentRepository commentRepository,  PostService postService, UserService userService) {
        this.commentRepository = commentRepository;
        this.postService = postService;
        this.userService = userService;
    }

    public List<Comment> getCommentsByArticleId(Long articleId){
        return commentRepository.findCommentsByPost_ArticleId(articleId);
    }


    @Transactional
    public boolean saveComment(Map<String,Object> delta, Long articleId, String commentUsername){
        try {
            String json = deltaToJsonConverter.convertDeltaToJson(delta);
            System.out.println("Converted JSON: " + json);
            Comment comment = new Comment();

            if (json == null || json.isBlank() || isOnlyWhiteSpaceOrEmpty(delta)) {
                System.out.println("Error: JSON content is null or empty");
                return false;
            }
            comment.setContent(json);
            comment.setPost(postService.getPostByArticle_id(articleId));
            comment.setCreation_time(new java.util.Date());
            comment.setAuthor(userService.getUserByUsername(commentUsername));
            commentRepository.save(comment);

            System.out.println("Content saved as: " + comment.getContent());
            return true;
        }catch (JsonProcessingException | Postdata_UpdateException e){
            System.out.println("Error processing JSON: " + e.getMessage());
            return false;
        }

    }

    public Map<String, Object> convertCommentStructure(String commentContent) throws JsonProcessingException {
        return deltaToJsonConverter.convertJsonToDelta(commentContent);
    }
    private boolean isOnlyWhiteSpaceOrEmpty(Map<String, Object> delta) {
        if (delta.containsKey("ops")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> ops = (List<Map<String, Object>>) delta.get("ops");
            for (Map<String, Object> op : ops) {
                if (op.containsKey("insert")) {
                    String text = (String)op.get("insert");
                    if (text != null && !text.trim().isEmpty() && !text.equals("\n")) {
                        return false; // 有实际内容
                    }
                }
            }
        }
        return true; // 无实际内容，只有空白或为空
    }

}
