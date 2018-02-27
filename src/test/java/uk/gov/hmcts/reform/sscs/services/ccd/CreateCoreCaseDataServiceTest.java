package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.sscs.CaseDataUtils;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;

@RunWith(MockitoJUnitRunner.class)
public class CreateCoreCaseDataServiceTest {

    @Mock
    private CoreCaseDataApi coreCaseDataApi;
    @Mock
    private CoreCaseDataService coreCaseDataService;

    private CreateCoreCaseDataService createCoreCaseDataService;

    @Before
    public void setUp() {
        createCoreCaseDataService = new CreateCoreCaseDataService(coreCaseDataApi, coreCaseDataService);
    }

    @Test
    public void givenACase_shouldSaveItIntoCcd() {
        //Given
        mockStartEventResponse();
        mockCaseDetails();
        when(coreCaseDataService.getEventRequestData(eq("appealCreated")))
            .thenReturn(EventRequestData.builder().build());
        when(coreCaseDataService.generateServiceAuthorization())
            .thenReturn("s2s token");
        when(coreCaseDataService.getCaseDataContent(
            any(CaseData.class),
            any(StartEventResponse.class),
            anyString(),
            anyString()
        )).thenReturn(CaseDataContent.builder().build());

        //When
        CaseDetails caseDetails = createCoreCaseDataService.createCcdCase(
            CaseDataUtils.buildCaseData("SC068/17/00013"));

        //Then
        assertNotNull(caseDetails);
        CaseData caseData = (CaseData) caseDetails.getData().get("caseReference");
        assertEquals("SC068/17/00013", caseData.getCaseReference());
    }

    private void mockCaseDetails() {
        Map<String, Object> caseData = new HashMap<>(1);
        caseData.put("caseReference", CaseDataUtils.buildCaseData("SC068/17/00013"));
        CaseDetails caseDetails = CaseDetails.builder().data(caseData).build();
        when(coreCaseDataApi.submitForCaseworker(anyString(), anyString(), anyString(), anyString(), anyString(),
            anyBoolean(), any(CaseDataContent.class))).thenReturn(caseDetails);
    }

    private void mockStartEventResponse() {
        when(coreCaseDataApi.startForCaseworker(anyString(), anyString(), anyString(), anyString(),
            anyString(), anyString())).thenReturn(StartEventResponse.builder()
            .caseDetails(CaseDetails.builder().build())
            .build());
    }

}
