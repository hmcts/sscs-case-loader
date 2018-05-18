package uk.gov.hmcts.reform.sscs.services.idam;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Base64;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.sscs.config.properties.IdamProperties;
import uk.gov.hmcts.reform.sscs.models.idam.Authorize;

@RunWith(MockitoJUnitRunner.class)
public class IdamServiceTest {

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private AuthTokenSubjectExtractor authTokenSubjectExtractor;
    @Mock
    private IdamApiClient idamApiClient;

    private Authorize authToken;
    private IdamProperties idamProperties;
    private IdamService idamService;

    @Before
    public void setUp() {
        authToken = new Authorize("redirect/", "authCode", "access");
        idamProperties = new IdamProperties();
        idamService = new IdamService(
            authTokenGenerator, authTokenSubjectExtractor, idamApiClient, idamProperties
        );
    }

    @Test
    public void shouldReturnAuthTokenGivenNewRequest() {
        String auth = "auth";
        when(authTokenGenerator.generate()).thenReturn(auth);
        assertThat(idamService.generateServiceAuthorization(), is(auth));
    }

    @Test
    public void shouldReturnServiceUserIdGivenAuthToken() {
        String auth = "auth";
        String serviceName = "sscs";
        when(authTokenSubjectExtractor.extract(auth)).thenReturn(serviceName);
        assertThat(idamService.getServiceUserId(auth), is(serviceName));
    }

    @Test
    public void shouldReturnIdamTokenGivenRequestForS2S() {
        IdamProperties.Oauth2 oauth2 = new IdamProperties.Oauth2();
        IdamProperties.Oauth2.User user = new IdamProperties.Oauth2.User();
        user.setEmail("email");
        user.setPassword("pass");
        oauth2.setUser(user);
        IdamProperties.Oauth2.Client client = new IdamProperties.Oauth2.Client();
        client.setId("id");
        client.setSecret("secret");
        oauth2.setClient(client);
        oauth2.setRedirectUrl("redirect/");
        idamProperties.setOauth2(oauth2);

        String base64Authorisation = Base64.getEncoder().encodeToString("email:pass".getBytes());

        when(idamApiClient.authorizeCodeType("Basic " + base64Authorisation,
            "code",
            "id",
            "redirect/")).thenReturn(authToken);

        when(idamApiClient.authorizeToken(authToken.getCode(),
            "authorization_code",
            "redirect/",
            "id",
            "secret")).thenReturn(authToken);

        String token = idamService.getIdamOauth2Token();

        assertThat(token, containsString("Bearer access"));
    }
}
