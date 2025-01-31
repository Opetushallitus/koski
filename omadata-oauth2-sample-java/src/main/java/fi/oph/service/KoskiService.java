package fi.oph.service;

import fi.oph.config.KoskiConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

@Service
public class KoskiService {

    private final KoskiConfig koskiConfig;
    private final OAuth2AuthorizedClientRepository authorizedClientRepository;
    private final RestClient restClient;

    public KoskiService(KoskiConfig koskiConfig, OAuth2AuthorizedClientRepository authorizedClientRepository,
                        @Qualifier(KoskiConfig.RESTCLIENT_ID) RestClient restClient) {
        this.koskiConfig = koskiConfig;
        this.authorizedClientRepository = authorizedClientRepository;
        this.restClient = restClient;
    }

    public String getRegistrationId() {
        return koskiConfig.getRegistrationId();
    }

    public OAuth2AuthorizedClient getAuthorizedClient(Authentication authentication, HttpServletRequest request) {
        return authorizedClientRepository.loadAuthorizedClient(koskiConfig.getRegistrationId(), authentication, request);
    }

    public void logout(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        authorizedClientRepository.removeAuthorizedClient(getRegistrationId(), authentication, request, response);
    }

    public String fetchDataFromResourceServer(OAuth2AuthorizedClient koski) {
        var accessToken = koski.getAccessToken().getTokenValue();
        return restClient.post()
                .uri(koskiConfig.getResourceServer())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .attributes(clientRegistrationId(koskiConfig.getRegistrationId()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
    }
}
