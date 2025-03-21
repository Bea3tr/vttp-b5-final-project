package vttp.project.server.repositories;

import java.time.Duration;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MailRepository {

    private static final Logger logger = Logger.getLogger(MailRepository.class.getName());

    @Autowired @Qualifier("myredis")
    private RedisTemplate<String, String> template;

    public void saveCode(String email, String code) {
        logger.info("[Mail Repo] Saving code for %s to Redis".formatted(email));
        template.opsForValue().set(email, code);
        template.expire(email, Duration.ofSeconds(60));
    }

    public String getCode(String email) {
        return template.opsForValue().get(email);
    }

    public boolean codeExists(String email) {
        return template.hasKey(email);
    }
    
}
