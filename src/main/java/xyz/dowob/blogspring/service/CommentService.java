package xyz.dowob.blogspring.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import xyz.dowob.blogspring.Exceptions.Commentdata_UpdateException;
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

    public Comment getCommentByCommentId(Long commentId) throws Commentdata_UpdateException {
        return commentRepository.findCommentByCommentId(commentId)
                .orElseThrow(() -> new Commentdata_UpdateException(Commentdata_UpdateException.ErrorCode.COMMENT_NOT_FOUND));
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

    public void editComment(Map<String, Object> delta, Long commentId, String currentUserId) throws Commentdata_UpdateException {

        try {
            Comment comment = getCommentByCommentId(commentId);
            String formattedCommentUserId = comment.getAuthor().getId().toString();
            if (!(formattedCommentUserId.equals(currentUserId))) {
                throw new Commentdata_UpdateException(Commentdata_UpdateException.ErrorCode.UNAUTHORIZED);
            }

            String json = deltaToJsonConverter.convertCommentDeltaToJson(delta);
            if (json == null || json.isBlank() || EditorMethod.isOnlyWhiteSpaceOrEmpty(delta)) {
                throw new Commentdata_UpdateException(Commentdata_UpdateException.ErrorCode.COMMENT_INVALID);
            }else if (json.length() > 10000){
                throw new Commentdata_UpdateException(Commentdata_UpdateException.ErrorCode.CONTENT_TOO_LONG);
            }

            comment.setContent(json);
            commentRepository.save(comment);

        } catch (JsonProcessingException e) {
            throw new Commentdata_UpdateException(Commentdata_UpdateException.ErrorCode.COMMENT_UPDATE_JSON_ERROR);
        }
    }

    public void deleteComment(Long commentId, String currentUserId) throws Commentdata_UpdateException {
        try {
            Comment comment = getCommentByCommentId(commentId);
            String formattedCommentUserId = comment.getAuthor().getId().toString();
            if (!(formattedCommentUserId.equals(currentUserId))) {
                throw new Commentdata_UpdateException(Commentdata_UpdateException.ErrorCode.UNAUTHORIZED);
            }
            comment.setDeleted(true);
            commentRepository.save(comment);
        } catch (Commentdata_UpdateException e) {
            throw new Commentdata_UpdateException(Commentdata_UpdateException.ErrorCode.COMMENT_NOT_FOUND);
        }
    }


    public Map<String, Object> convertCommentStructure(String commentContent, Comment comment) throws JsonProcessingException {
        return deltaToJsonConverter.convertCommentFromJsonToDelta(commentContent, comment);
    }

    public boolean isUserEditable(Long commentId, String currentUserId) {
        try {
            Comment comment = getCommentByCommentId(commentId);
            String formattedCommentUserId = comment.getAuthor().getId().toString();
            return formattedCommentUserId.equals(currentUserId);
        } catch (Commentdata_UpdateException e) {
            return false;
        }
    }
}

