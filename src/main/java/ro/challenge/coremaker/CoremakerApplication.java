package ro.challenge.coremaker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CoremakerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoremakerApplication.class, args);
	}

}
