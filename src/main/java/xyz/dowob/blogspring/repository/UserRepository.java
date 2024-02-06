package xyz.dowob.blogspring.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.dowob.blogspring.model.User;

import java.util.Optional;
@SpringBootApplication
@Repository
public interface UserRepository extends JpaRepository<User, Long>{

    Optional<User> findByUsername(String username);
    /*Optional<User> findByEmail(String email);*/
}

