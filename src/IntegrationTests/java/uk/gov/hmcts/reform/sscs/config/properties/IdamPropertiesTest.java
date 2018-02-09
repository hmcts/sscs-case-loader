package uk.gov.hmcts.reform.sscs.config.properties;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.TestCaseLoaderApp;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestCaseLoaderApp.class)
public class IdamPropertiesTest {
    @Autowired
    private IdamProperties idamProperties;

    @Test
    public void givenAnInstanceOfIdamProperties_shouldContentOauth2Values() {
        IdamProperties.Oauth2 oauth2 = idamProperties.getOauth2();
        assertNotNull(oauth2.getClient());
        assertNotNull(oauth2.getUser());
        assertNotNull(oauth2.getRedirectUrl());
    }
}
