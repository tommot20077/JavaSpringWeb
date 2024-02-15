package xyz.dowob.blogspring.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import xyz.dowob.blogspring.model.Post;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findByArticleId(Long article_id);

    @Query("SELECT p FROM Post p WHERE p.title LIKE %:keyword%")
    Page<Post> searchByTitle(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.author.id = :authorID")
    Page<Post> findByAuthorID(@Param("authorID") long id, Pageable pageable);
}
