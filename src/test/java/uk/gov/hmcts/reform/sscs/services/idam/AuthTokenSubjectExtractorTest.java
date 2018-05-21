package uk.gov.hmcts.reform.sscs.services.idam;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.exceptions.JwtDecodingException;

@RunWith(MockitoJUnitRunner.class)
public class AuthTokenSubjectExtractorTest {

    private AuthTokenSubjectExtractor authTokenSubjectExtractor;

    @Before
    public void setUp() {
        authTokenSubjectExtractor = new AuthTokenSubjectExtractor();
    }

    @Test
    public void shouldExtractSubjectFromJwt() {
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ"
                       + "zc2NzIiwiZXhwIjoxNTI2NjU2NTEyfQ."
                       + "aADJFE6_FJPNpDO_0NbqS-oYIDM9Bjjh"
                       + "18ZyB1imXGXAqOEc8Iyy0zxBe6BhXFl8"
                       + "E8panNAv3zdDDeOhlrEViQ";

        assertThat(authTokenSubjectExtractor.extract(token), is("sscs"));
    }

    @Test
    public void shouldExtractSubjectFromJwtWithBearerType() {
        String token = "Bearer "
                       + "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ"
                       + "zc2NzIiwiZXhwIjoxNTI2NjU2NTEyfQ."
                       + "aADJFE6_FJPNpDO_0NbqS-oYIDM9Bjjh"
                       + "18ZyB1imXGXAqOEc8Iyy0zxBe6BhXFl8"
                       + "E8panNAv3zdDDeOhlrEViQ";

        assertThat(authTokenSubjectExtractor.extract(token), is("sscs"));
    }

    @Test(expected = JwtDecodingException.class)
    public void shouldThrowForMalformedJwt() {
        authTokenSubjectExtractor.extract("badgers");
    }

}
