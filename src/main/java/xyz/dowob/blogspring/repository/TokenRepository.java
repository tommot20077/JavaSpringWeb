package xyz.dowob.blogspring.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import xyz.dowob.blogspring.model.VerificationToken;

import java.util.Date;
import java.util.List;

@Repository
public interface TokenRepository extends CrudRepository<VerificationToken, Long> {
    VerificationToken findByToken(String token);
    List<VerificationToken> findAllByExpiryDateLessThan(Date now);
}
