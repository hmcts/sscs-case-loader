package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.sscs.CaseDataUtils;
import uk.gov.hmcts.reform.sscs.config.properties.CoreCaseDataProperties;
import uk.gov.hmcts.reform.sscs.models.idam.Authorize;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.services.idam.IdamApiClient;

@RunWith(MockitoJUnitRunner.class)
public class CoreCaseDataServiceTest {

    @Mock
    private CoreCaseDataApi coreCaseDataApiMock;
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private CoreCaseDataProperties coreCaseDataPropertiesMock;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private IdamApiClient idamApiClient;

    @Before
    public void setUp() {
        coreCaseDataService = new CoreCaseDataService(coreCaseDataApiMock, coreCaseDataPropertiesMock,
            authTokenGenerator, idamApiClient);
    }

    @Test
    public void givenACase_shouldSaveItIntoCcd() {
        //Given
        mockCoreCaseDataProperties();
        mockStartEventResponse();
        mockCaseDetails();
        when(idamApiClient.authorize(anyString())).thenReturn(Authorize.builder().build());

        //When
        CaseDetails caseDetails = coreCaseDataService.startEventAndSaveGivenCase(CaseDataUtils.buildCaseData());

        //Then
        assertNotNull(caseDetails);
        CaseData caseData = (CaseData) caseDetails.getData().get("case-data");
        assertEquals("2017-10-08", caseData.getAppeal().getMrnDate());
    }

    private void mockCaseDetails() {
        Map<String, Object> caseData = new HashMap<>(1);
        caseData.put("case-data", CaseDataUtils.buildCaseData());
        CaseDetails caseDetails = CaseDetails.builder().data(caseData).build();
        when(coreCaseDataApiMock.submitForCaseworker(anyString(), anyString(), anyString(), anyString(), anyString(),
            eq(true), any(CaseDataContent.class))).thenReturn(caseDetails);
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
