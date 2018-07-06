package demo.admin;

import java.security.Principal;

import org.springframework.web.bind.annotation.ModelAttribute;

import de.codecentric.boot.admin.server.ui.web.UiController;
import de.codecentric.boot.admin.server.web.AdminController;

/**
 * Required to work around some serialization issues with the Keycloak principal.
 * @see #getUiSettings(Principal)
 * @author tom
 */
@AdminController
public class CustomUiController extends UiController {

	public CustomUiController(String adminContextPath, String title, String brand) {
		super(adminContextPath, title, brand);
	}

	@ModelAttribute(value = "user", binding = false)
	public Principal getUiSettings(Principal principal) {
		// HACK to avoid sending unserializable principal objects
		// Keycloaks Principal implementation contains some unserializable components
		// that cause serialization problems.
		return new Principal() {
			@Override
			public String getName() {
				return principal.getName();
			}
		};
	}
}
