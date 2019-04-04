package demo.todo;

import java.time.Instant;
import java.util.Arrays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableScheduling
@SpringBootApplication
public class TodoServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TodoServiceApplication.class, args);
	}

	@Scheduled(fixedRate = 5_000)
	public void doSomework() {

		// useful to demonstrate log dynamic level configuration
		log.info("work info");
		log.debug("work debug");
		log.trace("work trace");
		log.error("work error");
	}
}

@RestController
class TodoController {

	@GetMapping("/")
	Object getTodos() {
		return Arrays.asList("Prepare talk..." + Instant.now());
	}
}
