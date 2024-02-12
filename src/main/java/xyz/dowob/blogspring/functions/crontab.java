package xyz.dowob.blogspring.functions;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import xyz.dowob.blogspring.service.TokenService;
@Component
public class crontab {
    private final TokenService tokenService;

    public crontab(TokenService tokenService) {
        this.tokenService = tokenService;
    }


    @Scheduled(cron = "0 0 1 * * ?")
    public void cleanExpiredTokens(){
        tokenService.removeExpiredTokens();
    }

}
