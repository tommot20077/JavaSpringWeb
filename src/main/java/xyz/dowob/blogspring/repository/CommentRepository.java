package xyz.dowob.blogspring.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import xyz.dowob.blogspring.model.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends CrudRepository<Comment, Long>{
    List<Comment> findCommentsByPost_ArticleId(Long PostId);

}

