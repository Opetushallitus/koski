package fi.oph.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

@Configuration
public class KoskiConfig {

    public static final String RESTCLIENT_ID = "koski";
    private static final String REGISTRATION_ID = "koski";
    private static final String SSL_BUNDLE = "koski-ssl-bundle";

    private final String resourceServer;

    public KoskiConfig(@Value("${koski.resource-server.url}") String resourceServer) {
        this.resourceServer = resourceServer;
    }

    @Bean(RESTCLIENT_ID)
    public RestClient koskiRestClient(RestClient.Builder builder,
                                      OAuth2AuthorizedClientManager authorizedClientManager,
                                      SslBundles sslBundles) {
        var requestFactory = ClientHttpRequestFactoryBuilder.jdk()
                .build(ClientHttpRequestFactorySettings.defaults()
                        .withSslBundle(sslBundles.getBundle(SSL_BUNDLE))
                        .withConnectTimeout(Duration.ofSeconds(5))
                        .withReadTimeout(Duration.ofSeconds(10)));

        var messageConverters = List.of(
                new FormHttpMessageConverter(), new OAuth2AccessTokenResponseHttpMessageConverter(),
                new StringHttpMessageConverter(), new MappingJackson2HttpMessageConverter()
        );

        return builder
                .requestInterceptor(new OAuth2ClientHttpRequestInterceptor(authorizedClientManager))
                .requestFactory(requestFactory)
                .messageConverters(messageConverters)
                .defaultStatusHandler(new OAuth2ErrorResponseErrorHandler())
                .build();
    }

    public String getRegistrationId() {
        return REGISTRATION_ID;
    }

    public String getResourceServer() {
        return resourceServer;
    }
}
