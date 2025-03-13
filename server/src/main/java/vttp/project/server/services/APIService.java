package vttp.project.server.services;

import java.io.StringReader;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import vttp.project.server.repositories.APIRepository;

@Service
public class APIService {

    private static final String PF_URL = "https://api.petfinder.com/v2/oauth2/token";
    private static final Logger logger = Logger.getLogger(APIService.class.getName());

    @Autowired
    private APIRepository apiRepo;

    @Value("${petfinder.id}")
    private String id;

    @Value("${petfinder.secret}")
    private String secret;

    public JsonObject getPfToken() {
        try {
            String token = "";
            if (apiRepo.pfTokenExists()) {
                token = apiRepo.getPfToken();

            } else {
                JsonObject body = Json.createObjectBuilder()
                        .add("grant_type", "client_credentials")
                        .add("client_id", id)
                        .add("client_secret", secret)
                        .build();

                RequestEntity<String> req = RequestEntity.post(PF_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body.toString(), String.class);

                RestTemplate template = new RestTemplate();
                ResponseEntity<String> resp = template.exchange(req, String.class);

                String payload = resp.getBody();
                logger.info("[APISvc] Petfinder resp: " + payload);
                JsonObject obj = Json.createReader(new StringReader(payload))
                        .readObject();

                token = obj.getString("access_token");

                apiRepo.savePfToken(token, obj.getInt("expires_in"));
            }
            return Json.createObjectBuilder()
                        .add("message", "success")
                        .add("token", token)
                        .build();

        } catch (Exception ex) {
            return Json.createObjectBuilder()
                .add("message", ex.getMessage())
                .add("token", "")
                .build();
        }
    }
}
