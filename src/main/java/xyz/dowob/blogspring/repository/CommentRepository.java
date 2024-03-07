package xyz.dowob.blogspring.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import xyz.dowob.blogspring.model.Comment;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends CrudRepository<Comment, Long>{
    Page<Comment> findCommentsPageByPost_ArticleId(Long PostId, Pageable pageable);
    List<Comment> findCommentsListByPost_ArticleId(Long PostId);
    Optional<Comment> findCommentByCommentId(Long commentId);


}

