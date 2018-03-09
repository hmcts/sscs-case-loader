package uk.gov.hmcts.reform.sscs.services;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import java.io.IOException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.services.ccd.CreateCoreCaseDataService;
import uk.gov.hmcts.reform.sscs.services.ccd.UpdateCoreCaseDataService;

@ContextConfiguration
@TestPropertySource(properties = { "sscs.case.loader.ignoreCasesBeforeDate=2017-01-01" })
public class ProcessCaseTest extends MockCcdIdamServices {

    private static final int EXPECTED_NUMBER_OF_CASES_TO_CREATE_IN_CCD = 1;
    private static final int EXPECTED_NUMBER_OF_CASES_TO_UPDATE_IN_CCD = 16;

    @SpyBean
    private CreateCoreCaseDataService createCoreCaseDataService;
    @SpyBean
    private UpdateCoreCaseDataService updateCoreCaseDataService;

    @Autowired
    private CaseLoaderService caseLoaderService;

    @Test
    public void shouldBeSavedIntoCcdGivenDeltaXmlInSftp() throws JSchException, SftpException, IOException {
        mockSftp();

        caseLoaderService.process();

        verify(createCoreCaseDataService, times(EXPECTED_NUMBER_OF_CASES_TO_CREATE_IN_CCD))
            .createCcdCase(any(CaseData.class));

        verify(coreCaseDataApi, times(EXPECTED_NUMBER_OF_CASES_TO_CREATE_IN_CCD))
            .submitForCaseworker(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                eq(Boolean.TRUE),
                any(CaseDataContent.class)
            );

        verify(updateCoreCaseDataService, times(EXPECTED_NUMBER_OF_CASES_TO_UPDATE_IN_CCD))
            .updateCase(any(CaseData.class), anyLong(), anyString());

        verify(coreCaseDataApi, times(EXPECTED_NUMBER_OF_CASES_TO_UPDATE_IN_CCD))
            .submitEventForCaseWorker(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                eq(Boolean.TRUE),
                any(CaseDataContent.class)
            );
    }
}
