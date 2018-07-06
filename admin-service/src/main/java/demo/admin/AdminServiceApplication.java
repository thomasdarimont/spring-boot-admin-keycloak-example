package demo.admin;

import java.util.Collection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import de.codecentric.boot.admin.server.config.EnableAdminServer;
import de.codecentric.boot.admin.server.ui.config.AdminServerUiProperties;
import de.codecentric.boot.admin.server.web.client.CompositeHttpHeadersProvider;
import de.codecentric.boot.admin.server.web.client.HttpHeadersProvider;

@EnableAdminServer
@SpringBootApplication
public class AdminServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdminServiceApplication.class, args);
	}

	@Bean
	public CustomUiController homeUiController( //
			AdminServerProperties properties, //
			AdminServerUiProperties uiProperties //
	) {
		return new CustomUiController(properties.getContextPath(), uiProperties.getTitle(), uiProperties.getBrand());
	}
}
