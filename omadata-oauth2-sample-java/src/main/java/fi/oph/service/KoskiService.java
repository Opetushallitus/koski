package fi.oph.service;

import fi.oph.config.KoskiConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.stereotype.Service;

@Service
public class KoskiService {

    private final KoskiConfig koskiConfig;

    private final OAuth2AuthorizedClientRepository authorizedClientRepository;

    public KoskiService(KoskiConfig koskiConfig, OAuth2AuthorizedClientRepository authorizedClientRepository) {
        this.koskiConfig = koskiConfig;
        this.authorizedClientRepository = authorizedClientRepository;
    }

    public String getRegistrationId() {
        return koskiConfig.getRegistrationId();
    }

    public OAuth2AuthorizedClient getAuthorizedClient(Authentication authentication, HttpServletRequest request) {
        return authorizedClientRepository.loadAuthorizedClient(koskiConfig.getRegistrationId(), authentication, request);
    }
}
