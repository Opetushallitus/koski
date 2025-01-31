package fi.oph.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestClient;

import static fi.oph.config.KoskiConfig.RESTCLIENT_ID;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain oauthConfig(HttpSecurity http, ClientRegistrationRepository repository,
                                           @Qualifier(RESTCLIENT_ID) RestClient oauthRestClient,
                                           HttpSessionOAuth2AuthorizedClientRepository authorizedClientRepository
    ) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for development purposes only. Don't do this in production!
                .securityMatcher(
                        "/",
                        "/error",
                        "/oauth2/**",
                        "/oauth2/logout/koski",
                        "/api/openid-api-test/form-post-response-cb" //OAuth2 callback URL
                )
                .oauth2Client(
                        client -> {
                            client.authorizationCodeGrant(
                                    code -> {
                                        code.authorizationRequestResolver(createKoskiAuthorizationRequestResolver(repository));
                                        code.accessTokenResponseClient(createAccessTokenResponseClient(oauthRestClient));
                                    });
                            client.authorizedClientRepository(authorizedClientRepository);
                        })
                .build();
    }

    private static RestClientAuthorizationCodeTokenResponseClient createAccessTokenResponseClient(RestClient oauthRestClient) {
        var responseClient = new RestClientAuthorizationCodeTokenResponseClient();
        responseClient.setRestClient(oauthRestClient);
        return responseClient;
    }

    private static DefaultOAuth2AuthorizationRequestResolver createKoskiAuthorizationRequestResolver(ClientRegistrationRepository repository) {
        var resolver =
                new DefaultOAuth2AuthorizationRequestResolver(
                        repository,
                        OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI);
        resolver.setAuthorizationRequestCustomizer(
                OAuth2AuthorizationRequestCustomizers.withPkce()
                        .andThen(customizer -> customizer.additionalParameters(
                                additionalParameters -> additionalParameters.put("response_mode", "form_post"))));
        return resolver;
    }

    @Bean
    public HttpSessionOAuth2AuthorizedClientRepository authorizedClientRepository() {
        return new HttpSessionOAuth2AuthorizedClientRepository();
    }
}

