package xyz.dowob.blogspring.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import xyz.dowob.blogspring.Exceptions.Postdata_UpdateException;
import xyz.dowob.blogspring.functions.DeltaToJsonConverter;
import xyz.dowob.blogspring.functions.EditorMethod;
import xyz.dowob.blogspring.model.Comment;
import xyz.dowob.blogspring.repository.CommentRepository;

import java.io.IOException;
import java.util.Map;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostService postService;
    private final UserService userService;
    private final DeltaToJsonConverter deltaToJsonConverter = new DeltaToJsonConverter();
    @Autowired
    public CommentService(CommentRepository commentRepository,  PostService postService, UserService userService) {
        this.commentRepository = commentRepository;
        this.postService = postService;
        this.userService = userService;
    }

    public Page<Comment> getCommentsByArticleId(Long articleId, Pageable pageable){
        return commentRepository.findCommentsPageByPost_ArticleId(articleId , pageable);
    }


    @Transactional
    public boolean saveComment(Map<String,Object> delta, Long articleId, String commentUsername){
        try {
            String json = deltaToJsonConverter.convertCommentDeltaToJson(delta);
            Comment comment = new Comment();

            if (json == null || json.isBlank() || EditorMethod.isOnlyWhiteSpaceOrEmpty(delta)) {
                return false;
            }

            comment.setContent(json);
            comment.setPost(postService.getPostByArticle_id(articleId));
            comment.setCreation_time(new java.util.Date());
            comment.setAuthor(userService.getUserByUsername(commentUsername));
            comment.setCommentInArticleId(commentRepository.findCommentsListByPost_ArticleId(articleId).size() + 1);
            comment.setDeleted(false);
            commentRepository.save(comment);
            return true;
        }catch (JsonProcessingException | Postdata_UpdateException e){
            return false;
        }

    }

    public String saveCommentImage(MultipartFile file, Long articleId) throws IOException {
        return EditorMethod.saveCommentImage(file, articleId);
    }

    public Map<String, Object> convertCommentStructure(String commentContent) throws JsonProcessingException {
        return deltaToJsonConverter.convertJsonToDelta(commentContent);
    }
}

