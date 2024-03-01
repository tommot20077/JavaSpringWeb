package xyz.dowob.blogspring.repository;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import xyz.dowob.blogspring.model.User;

import java.util.Optional;
@SpringBootApplication
@Repository
public interface UserRepository extends JpaRepository<User, Long>{

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    Optional<User> findByPostsArticleId(Long postId);

    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword%")
    Page<User> searchByUsernameOrEmail(@Param("keyword") String keyword, Pageable pageable);

}

