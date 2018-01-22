package uk.gov.hmcts.reform.sscs.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.sscs.config.properties.CoreCaseDataProperties;
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
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private CoreCaseDataProperties coreCaseDataPropertiesMock;

    @Before
    public void setUp() {
        coreCaseDataService = new CoreCaseDataService(coreCaseDataApiMock, coreCaseDataPropertiesMock);
    }

    @Test
    public void givenACase_shouldSaveItIntoCcd() {
        mockCoreCaseDataProperties();
        mockStartEventResponse();
        mockCaseDetails();
        CcdCase ccdCase = CcdCase.builder().caseRef("SC0001").build();
        CaseDetails caseDetails = coreCaseDataService.startEventAndSaveGivenCase(ccdCase);
        assertNotNull(caseDetails);
    }

    private void mockCaseDetails() {
        CaseDetails caseDetailsMock = mock(CaseDetails.class);
        when(coreCaseDataApiMock.submitForCaseworker(anyString(), anyString(), anyString(), anyString(), anyString(),
            eq(true), any(CaseDataContent.class))).thenReturn(caseDetailsMock);
    }

    private void mockStartEventResponse() {
        StartEventResponse startEventResponseMock = mock(StartEventResponse.class);
        when(coreCaseDataApiMock.startForCaseworker(anyString(), anyString(), anyString(), anyString(),
            anyString(), anyString())).thenReturn(startEventResponseMock);
    }

    private void mockCoreCaseDataProperties() {
        when(coreCaseDataPropertiesMock.getUserId()).thenReturn("userId");
        when(coreCaseDataPropertiesMock.getJurisdictionId()).thenReturn("jurisdictionId");
        when(coreCaseDataPropertiesMock.getCaseTypeId()).thenReturn("caseTypeId");
        when(coreCaseDataPropertiesMock.getEventId()).thenReturn("eventId");
    }
}
