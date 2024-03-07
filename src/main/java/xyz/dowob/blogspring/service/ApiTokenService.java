package xyz.dowob.blogspring.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.dowob.blogspring.model.ApiToken;
import xyz.dowob.blogspring.repository.ApiTokenRepository;

import java.util.Optional;

@Service
public class ApiTokenService {
    private final ApiTokenRepository apiTokenRepository;


    @Autowired
    public ApiTokenService(ApiTokenRepository apiTokenRepository) {
        this.apiTokenRepository = apiTokenRepository;
    }


@Transactional
    public boolean incrementTokenAndCheckLimit(String ipAddress) {
         final int MAX_REQUESTS_PER_HOUR = 30;
        Optional<ApiToken> optionalApiToken  = apiTokenRepository.findApiTokenByIpAddress(ipAddress);
        ApiToken token;
        if (optionalApiToken.isPresent()) {
            token = optionalApiToken.get();
        } else {
            token = new ApiToken(ipAddress);
            apiTokenRepository.save(token);
        }

        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - token.getLastResetTime();

        if (elapsedTime > 1000 * 60 * 60) {
            token.resetRequestCount();
        }

        if (token.getRequestCount() < MAX_REQUESTS_PER_HOUR) {
            token.incrementRequestCount();
            apiTokenRepository.save(token);
            return true;
        } else {
            return false;
        }
    }
}
