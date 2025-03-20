package vttp.project.server.repositories;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MailRepository {

    @Autowired
    private RedisTemplate<String, String> template;

    public void saveCode(String email, String code) {
        template.opsForValue().set(email, code);
        template.expire(email, Duration.ofSeconds(30));
    }

    public String getCode(String email) {
        return template.opsForValue().get(email);
    }

    public boolean codeExists(String email) {
        return template.hasKey(email);
    }
    
}
