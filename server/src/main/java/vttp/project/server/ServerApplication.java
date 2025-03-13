package vttp.project.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import vttp.project.server.services.APIService;
import vttp.project.server.services.UserService;

@SpringBootApplication
public class ServerApplication implements CommandLineRunner {

	@Autowired
	private UserService userSvc;

	@Autowired
	private APIService apiSvc;
	
	public static void main(String[] args) {
		SpringApplication.run(ServerApplication.class, args);
	}

	@Override
	public void run(String... args) {
		// try {
		// 	byte[] defaultImg = Files.readAllBytes(Paths.get("src/main/resources/static/user.png"));
		// } catch (IOException ex) {
		// 	ex.printStackTrace();
		// }
	}

}
