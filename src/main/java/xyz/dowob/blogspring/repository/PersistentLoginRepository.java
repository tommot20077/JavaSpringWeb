package xyz.dowob.blogspring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.dowob.blogspring.model.PersistentLogin;

import java.util.Optional;

public interface PersistentLoginRepository extends JpaRepository<PersistentLogin, Long> {
    Optional<PersistentLogin> findByToken(String token);
    Optional<PersistentLogin> findByUser_Id(long user_id);
}
