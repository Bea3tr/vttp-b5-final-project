package vttp.project.server.repositories;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class APIRepository {

    @Autowired @Qualifier("myredis")
    private RedisTemplate<String, String> template;

    public boolean pfTokenExists() {
        return template.hasKey("pf_access_token");
    }

    public String getPfToken() {
        return template.opsForValue().get("pf_access_token");
    }

    public void savePfToken(String token, int expire) {
        template.opsForValue().set("pf_access_token", token);
        // Remove from db 1 min before token expires
        template.expire("pf_access_token", Duration.ofSeconds(expire - 60));
    }
    
}
