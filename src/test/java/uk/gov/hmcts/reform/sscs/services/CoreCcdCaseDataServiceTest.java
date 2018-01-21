package uk.gov.hmcts.reform.sscs.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.sscs.models.CcdCase;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CoreCcdCaseDataServiceTest {

    @Mock
    private CoreCaseDataApi coreCaseDataApiMock;

    @Test
    public void givenACase_shouldSaveItIntoCcd() {
        CoreCaseDataService coreCaseDataService = new CoreCaseDataService(coreCaseDataApiMock);
        CcdCase ccdCase = CcdCase.builder().caseRef("SC0001").build();

        StartEventResponse startEventResponseMock = mock(StartEventResponse.class);
        when(coreCaseDataApiMock.startForCaseworker(anyString(), anyString(), anyString(), anyString(),
            anyString(), anyString())).thenReturn(startEventResponseMock);

        CaseDetails caseDetailsMock = mock(CaseDetails.class);
        when(coreCaseDataApiMock.submitForCaseworker(anyString(), anyString(), anyString(), anyString(), anyString(),
            eq(true), any(CaseDataContent.class))).thenReturn(caseDetailsMock);

        CaseDetails caseDetails = coreCaseDataService.startEventAndSaveGivenCase(ccdCase);

        assertNotNull(caseDetails);
    }
}
