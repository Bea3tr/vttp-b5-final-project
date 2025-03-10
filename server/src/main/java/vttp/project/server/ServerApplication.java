package vttp.project.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import vttp.project.server.services.UserService;

@SpringBootApplication
public class ServerApplication implements CommandLineRunner {

	@Autowired
	private UserService userSvc;
	
	public static void main(String[] args) {
		SpringApplication.run(ServerApplication.class, args);
	}

	@Override
	public void run(String... args) {
	}

}
