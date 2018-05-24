package demo.admin.keycloak;

import java.security.Principal;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import de.codecentric.boot.admin.web.client.HttpHeadersProvider;

@KeycloakConfiguration
@EnableConfigurationProperties(KeycloakSpringBootProperties.class)
class KeycloakConfig extends KeycloakWebSecurityConfigurerAdapter {

	/**
	 * {@link HttpHeadersProvider} used to populate the {@link HttpHeaders} for
	 * accessing the state of the disovered clients.
	 * 
	 * @param keycloak
	 * @return
	 */
	@Bean
	public HttpHeadersProvider httpHeadersProvider(Keycloak keycloak) {
		return (app) -> {
			String accessToken = keycloak.tokenManager().getAccessTokenString();
			HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", "Bearer " + accessToken);
			return headers;
		};
	}

	/**
	 * The Keycloak Admin client that provides the service-account Access-Token
	 * 
	 * @param props
	 * @return
	 */
	@Bean
	public Keycloak keycloak(KeycloakSpringBootProperties props) {
		return KeycloakBuilder.builder() //
				.serverUrl(props.getAuthServerUrl()) //
				.realm(props.getRealm()) //
				.grantType(OAuth2Constants.CLIENT_CREDENTIALS) //
				.clientId(props.getResource()) //
				.clientSecret((String) props.getCredentials().get("secret")) //
				.build();
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		super.configure(http);

		http //
				.csrf().disable() //
				.authorizeRequests() //
				.antMatchers("/**/*.css", "/admin/img/**", "/admin/third-party/**").permitAll() //
				.antMatchers("/admin").hasRole("ADMIN") //
				.anyRequest().permitAll() //
		;
	}

	/**
	 * Load Keycloak configuration from application.properties or application.yml
	 * 
	 * @return
	 */
	@Bean
	public KeycloakConfigResolver keycloakConfigResolver() {
		return new KeycloakSpringBootConfigResolver();
	}

	/**
	 * Use {@link KeycloakAuthenticationProvider}
	 * 
	 * @param auth
	 * @throws Exception
	 */
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {

		SimpleAuthorityMapper grantedAuthorityMapper = new SimpleAuthorityMapper();
		grantedAuthorityMapper.setPrefix("ROLE_");
		grantedAuthorityMapper.setConvertToUpperCase(true);

		KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
		keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(grantedAuthorityMapper);
		auth.authenticationProvider(keycloakAuthenticationProvider);
	}

	@Bean
	@Override
	protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
		return new RegisterSessionAuthenticationStrategy(buildSessionRegistry());
	}

	@Bean
	protected SessionRegistry buildSessionRegistry() {
		return new SessionRegistryImpl();
	}

	/**
	 * Allows to inject requests scoped wrapper for {@link KeycloakSecurityContext}.
	 * 
	 * Returns the {@link KeycloakSecurityContext} from the Spring
	 * {@link ServletRequestAttributes}'s {@link Principal}.
	 * <p>
	 * The principal must support retrieval of the KeycloakSecurityContext, so at
	 * this point, only {@link KeycloakPrincipal} values and
	 * {@link KeycloakAuthenticationToken} are supported.
	 *
	 * @return the current <code>KeycloakSecurityContext</code>
	 */
	@Bean
	@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
	public KeycloakSecurityContext provideKeycloakSecurityContext() {

		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		Principal principal = attributes.getRequest().getUserPrincipal();
		if (principal == null) {
			return null;
		}

		if (principal instanceof KeycloakAuthenticationToken) {
			principal = Principal.class.cast(KeycloakAuthenticationToken.class.cast(principal).getPrincipal());
		}

		if (principal instanceof KeycloakPrincipal) {
			return KeycloakPrincipal.class.cast(principal).getKeycloakSecurityContext();
		}

		return null;
	}
}
