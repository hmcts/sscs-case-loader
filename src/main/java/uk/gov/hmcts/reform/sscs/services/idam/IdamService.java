package uk.gov.hmcts.reform.sscs.services.idam;

import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.sscs.config.properties.IdamProperties;
import uk.gov.hmcts.reform.sscs.models.idam.Authorize;

@Service
@Slf4j
public class IdamService {
    private final AuthTokenGenerator authTokenGenerator;
    private final IdamApiClient idamApiClient;
    private final IdamProperties idamProperties;

    @Autowired
    public IdamService(AuthTokenGenerator authTokenGenerator, IdamApiClient idamApiClient,
                       IdamProperties idamProperties) {
        this.authTokenGenerator = authTokenGenerator;
        this.idamApiClient = idamApiClient;
        this.idamProperties = idamProperties;
    }

    public String generateServiceAuthorization() {
        String s2sToken = authTokenGenerator.generate();
        log.info("s2s Token: {}", s2sToken);
        return s2sToken;
    }

    public String getIdamOauth2Token() {
        log.info("getIdamOauth2Token...");
        String authorisation = idamProperties.getOauth2().getUser().getEmail()
            + ":" + idamProperties.getOauth2().getUser().getPassword();
        String base64Authorisation = Base64.getEncoder().encodeToString(authorisation.getBytes());

        Authorize authorize = idamApiClient.authorizeCodeType(
            "Basic " + base64Authorisation,
            "code",
            idamProperties.getOauth2().getClient().getId(),
            idamProperties.getOauth2().getRedirectUrl()
        );

        Authorize authorizeToken = idamApiClient.authorizeToken(
            authorize.getCode(),
            "authorization_code",
            idamProperties.getOauth2().getRedirectUrl(),
            idamProperties.getOauth2().getClient().getId(),
            idamProperties.getOauth2().getClient().getSecret()
        );

        String oauth2Token = "Bearer " + authorizeToken.getAccessToken();
        log.info("oauth2Token: " + oauth2Token);
        return oauth2Token;
    }
}
