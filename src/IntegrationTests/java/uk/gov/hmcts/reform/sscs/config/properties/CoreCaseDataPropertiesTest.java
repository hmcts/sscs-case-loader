package uk.gov.hmcts.reform.sscs.config.properties;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CoreCaseDataPropertiesTest {
    @Autowired
    private CoreCaseDataProperties coreCaseDataProperties;

    @Test
    public void givenAnInstance_shouldLoadAllProperties() {
        assertNotNull(coreCaseDataProperties.getApi().getUrl());
        assertNotNull(coreCaseDataProperties.getUserId());
        assertNotNull(coreCaseDataProperties.getJurisdictionId());
        assertNotNull(coreCaseDataProperties.getCaseTypeId());
    }
}
