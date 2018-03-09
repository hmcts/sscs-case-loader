package uk.gov.hmcts.reform.sscs.services;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.sscs.CaseDataUtils;
import uk.gov.hmcts.reform.sscs.services.ccd.CreateCoreCaseDataService;
import uk.gov.hmcts.reform.sscs.services.ccd.UpdateCoreCaseDataService;

public class CoreCddCaseDataServiceTest extends MockCcdIdamServices {

    @Autowired
    private CreateCoreCaseDataService createCoreCaseDataService;
    @Autowired
    private UpdateCoreCaseDataService updateCoreCaseDataService;

    @Test
    public void givenACase_shouldSaveItIntoCdd() {

        createCoreCaseDataService.createCcdCase(CaseDataUtils.buildCaseData("SC068/17/00013"));

        verify(coreCaseDataApi).startForCaseworker(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString()
        );

        verify(coreCaseDataApi).submitForCaseworker(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            eq(true),
            any(CaseDataContent.class)
        );

    }

    @Test
    public void givenACase_shouldUpdateItInCdd() {

        updateCoreCaseDataService.updateCase(CaseDataUtils
            .buildCaseData("SC068/17/00013"), 123L, "appealReceived");

        verify(coreCaseDataApi).startEventForCaseWorker(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString()
        );

        verify(coreCaseDataApi).submitEventForCaseWorker(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            eq(true),
            any(CaseDataContent.class)
        );

    }
}
