package fi.oph.controller;

import fi.oph.service.KoskiService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController("/")
public class KoskiController {

    private static final String HOME_URL = "/";

    private final KoskiService koskiService;

    public KoskiController(KoskiService koskiService) {
        this.koskiService = koskiService;
    }

    @GetMapping(HOME_URL)
    public String home(Authentication authentication, HttpServletRequest request) {
        var authorizedClient = koskiService.getAuthorizedClient(authentication, request);
        if (authorizedClient == null) {
            return getUserNotAuthenticatedHtml();
        }
        var data = request.getServletContext().getAttribute("data");
        return getUserAuthenticatedHtml(authorizedClient, data == null ? null : data.toString());
    }

    private String getUserNotAuthenticatedHtml() {
        return """
                <center>
                <h1><a href="/oauth2/authorization/%s">Login and authorize</a></h1>
                <br/>You have NOT authorized use of your data.<br/>
                </center>
                """.formatted(koskiService.getRegistrationId());
    }

    private String getUserAuthenticatedHtml(OAuth2AuthorizedClient authorizedClient, String fetchedData) {
        var accessToken = authorizedClient.getAccessToken();
        return """
                <center>
                <h1><a href="/oauth2/logout/%s">Logout</a></h1>
                <br/>You have authorized use of your data.<br/>
                <br/>Access token: %s
                <br/>Issued at: %s
                <br/>Expires at: %s
                <br/>Scopes: %s
                <br/>
                <br/>Retrieved data from resource server:
                <br/><code>%s</code>
                </center>
                """.formatted(koskiService.getRegistrationId(),
                accessToken.getTokenValue(), accessToken.getIssuedAt(), accessToken.getExpiresAt(),
                String.join(", ", authorizedClient.getClientRegistration().getScopes()),
                fetchedData == null ? "" : fetchedData);
    }

    /**
     * OAuth2 authorization code flow callback endpoint. This endpoint gets triggered when
     * the OAuth2 authorization code flow is completed upon either fail or success.
     * It is defined in application.yml the OAuth2 client registration.
     *
     * @param authentication {@link Authentication}
     * @param request {@link HttpServletRequest}
     * @param response {@link HttpServletResponse}
     * @throws IOException {@link IOException}
     * @throws ServletException {@link ServletException}
     */
    @GetMapping("/api/openid-api-test/form-post-response-cb")
    public void oAuth2DoneCallbackEndpoint(Authentication authentication,
                                           HttpServletRequest request, HttpServletResponse response
    ) throws IOException, ServletException {
        var authorizedClient = koskiService.getAuthorizedClient(authentication, request);
        if (authorizedClient == null) {
            response.sendRedirect(HOME_URL);
            return;
        }
        var servletContext = request.getServletContext();
        var jsonData = koskiService.fetchDataFromResourceServer(authorizedClient);
        servletContext.setAttribute("data", jsonData);
        var dispatcher = servletContext.getRequestDispatcher(HOME_URL);
        dispatcher.forward(request, response);
    }

    @GetMapping("/oauth2/logout/koski")
    public void logoutGet(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws IOException {
        koskiService.logout(authentication, request, response);
        response.sendRedirect(HOME_URL);
    }
}

