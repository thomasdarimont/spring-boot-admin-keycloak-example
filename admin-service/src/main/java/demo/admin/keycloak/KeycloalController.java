package demo.admin.keycloak;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
class KeycloalController {

	@PostMapping("/admin/logout")
	public String logout(HttpServletRequest request) throws Exception {
		request.logout();
		return "redirect:/admin";
	}
}
