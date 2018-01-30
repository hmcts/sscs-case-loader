package uk.gov.hmcts.reform.sscs.services;

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
import uk.gov.hmcts.reform.sscs.models.CaseData;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
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
    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Before
    public void setUp() {
        coreCaseDataService = new CoreCaseDataService(coreCaseDataApiMock, coreCaseDataPropertiesMock,
            authTokenGenerator);
    }

    @Test
    public void givenACase_shouldSaveItIntoCcd() {
        //Given
        mockCoreCaseDataProperties();
        mockStartEventResponse();
        mockCaseDetails();

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
