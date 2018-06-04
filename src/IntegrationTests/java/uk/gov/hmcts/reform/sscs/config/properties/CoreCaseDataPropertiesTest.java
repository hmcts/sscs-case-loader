package uk.gov.hmcts.reform.sscs.config.properties;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpChannelAdapter;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CoreCaseDataPropertiesTest {

    @MockBean
    SftpChannelAdapter channelAdapter;

    @Autowired
    private CoreCaseDataProperties coreCaseDataProperties;

    @Test
    public void shouldLoadAllPropertiesGivenAnInstance() {
        assertNotNull(coreCaseDataProperties.getApi().getUrl());
        assertNotNull(coreCaseDataProperties.getJurisdictionId());
        assertNotNull(coreCaseDataProperties.getCaseTypeId());
    }
}
