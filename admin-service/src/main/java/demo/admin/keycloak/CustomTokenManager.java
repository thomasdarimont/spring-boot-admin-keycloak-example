package demo.admin.keycloak;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.keycloak.admin.client.Config;
import org.keycloak.admin.client.resource.BasicAuthFilter;
import org.keycloak.admin.client.token.TokenManager;
import org.keycloak.admin.client.token.TokenService;
import org.keycloak.common.util.Time;
import org.keycloak.representations.AccessTokenResponse;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Form;

import static org.keycloak.OAuth2Constants.*;

/**
 * A TokenManager that also supports scope offline_access.
 * <p>
 * Copied from {@link org.keycloak.admin.client.token.TokenManager}
 */
public class CustomTokenManager extends TokenManager {
    private static final long DEFAULT_MIN_VALIDITY = 30;

    private AccessTokenResponse currentToken;
    private long expirationTime;
    private long minTokenValidity = DEFAULT_MIN_VALIDITY;
    private final CustomConfig config;
    private final TokenService tokenService;
    private final String accessTokenGrantType;

    public CustomTokenManager(CustomConfig config, ResteasyClient client) {
        super(config, client);

        this.config = config;
        ResteasyWebTarget target = client.target(config.getServerUrl());
        if (!config.isPublicClient()) {
            target.register(new BasicAuthFilter(config.getClientId(), config.getClientSecret()));
        }
        this.tokenService = target.proxy(TokenService.class);
        this.accessTokenGrantType = config.getGrantType();

        if (CLIENT_CREDENTIALS.equals(accessTokenGrantType) && config.isPublicClient()) {
            throw new IllegalArgumentException("Can't use " + GRANT_TYPE + "=" + CLIENT_CREDENTIALS + " with public client");
        }
    }

    public String getAccessTokenString() {
        return getAccessToken().getToken();
    }

    public synchronized AccessTokenResponse getAccessToken() {
        if (currentToken == null) {
            grantToken();
        } else if (tokenExpired()) {
            refreshToken();
        }
        return currentToken;
    }

    public AccessTokenResponse grantToken() {
        Form form = new Form().param(GRANT_TYPE, accessTokenGrantType);
        if (PASSWORD.equals(accessTokenGrantType)) {
            form.param("username", config.getUsername())
                    .param("password", config.getPassword());
        }

        if (config.isPublicClient()) {
            form.param(CLIENT_ID, config.getClientId());
        }

        if (config.isOfflineAccess()) {
            form.param(SCOPE, OFFLINE_ACCESS);
        }

        int requestTime = Time.currentTime();
        synchronized (this) {
            currentToken = tokenService.grantToken(config.getRealm(), form.asMap());
            expirationTime = requestTime + currentToken.getExpiresIn();
        }
        return currentToken;
    }

    public synchronized AccessTokenResponse refreshToken() {
        Form form = new Form().param(GRANT_TYPE, REFRESH_TOKEN)
                .param(REFRESH_TOKEN, currentToken.getRefreshToken());

        if (config.isPublicClient()) {
            form.param(CLIENT_ID, config.getClientId());
        }

        try {
            int requestTime = Time.currentTime();

            currentToken = tokenService.refreshToken(config.getRealm(), form.asMap());
            expirationTime = requestTime + currentToken.getExpiresIn();
            return currentToken;
        } catch (BadRequestException e) {
            return grantToken();
        }
    }

    public synchronized void setMinTokenValidity(long minTokenValidity) {
        this.minTokenValidity = minTokenValidity;
    }

    private synchronized boolean tokenExpired() {
        return (Time.currentTime() + minTokenValidity) >= expirationTime;
    }

    /**
     * Invalidates the current token, but only when it is equal to the token passed as an argument.
     *
     * @param token the token to invalidate (cannot be null).
     */
    public synchronized void invalidate(String token) {
        if (currentToken == null) {
            return; // There's nothing to invalidate.
        }
        if (token.equals(currentToken.getToken())) {
            // When used next, this cause a refresh attempt, that in turn will cause a grant attempt if refreshing fails.
            expirationTime = -1;
        }
    }

    public static class CustomConfig extends Config {

        private boolean offlineAccess;

        public CustomConfig(Config config) {
            super(config.getServerUrl(), config.getRealm(), config.getUsername(), config.getPassword(), config.getClientId(), config.getClientSecret());
            this.setGrantType(config.getGrantType());
        }

        public boolean isOfflineAccess() {
            return offlineAccess;
        }

        public void setOfflineAccess(boolean offlineAccess) {
            this.offlineAccess = offlineAccess;
        }
    }
}
