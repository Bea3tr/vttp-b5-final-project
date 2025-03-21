package vttp.project.server.controllers;

import java.io.StringReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.mail.MessagingException;
import vttp.project.server.services.MailService;

@RestController
@RequestMapping(path="/api/mail")
public class MailController {

    @Autowired
    private MailService mailSvc;

    @PostMapping("/send-reset-code")
    public ResponseEntity<String> sendResetCode(@RequestBody String payload) throws MessagingException {
        JsonObject inObj = Json.createReader(new StringReader(payload))
                .readObject().getJsonObject("body");
        String email = inObj.getString("email");
        try {
            String code = mailSvc.generateVerificationCode();
            mailSvc.sendVerificationEmail(email, code);
            
            return ResponseEntity.ok(Json.createObjectBuilder()
                .add("message","Verification code sent successfully")
                .build().toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(400)
                .body(Json.createObjectBuilder()
                    .add("message","Error sending verification code. Please ensure your email is valid and resend the code.")
                    .build().toString());
        }
    }

    @PostMapping("/verify-code")
    public ResponseEntity<String> verifyCode(@RequestBody String payload) {
        JsonObject inObj = Json.createReader(new StringReader(payload))
                .readObject().getJsonObject("body");
        String email = inObj.getString("email");
        String code = inObj.getString("code");
        boolean verified = mailSvc.codeExists(email) && mailSvc.getCode(email).equals(code);
        if(verified) {
            return ResponseEntity.ok(Json.createObjectBuilder()
                .add("Verified", verified)
                .build().toString());
        }
        return ResponseEntity.status(401)
            .body(Json.createObjectBuilder()
                .add("message", "Invalid verification code")
                .build().toString());
    }
}
