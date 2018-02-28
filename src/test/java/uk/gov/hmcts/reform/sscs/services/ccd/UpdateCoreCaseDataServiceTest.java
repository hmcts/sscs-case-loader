package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
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
public class UpdateCoreCaseDataServiceTest {

    @Mock
    private CoreCaseDataApi coreCaseDataApi;
    @Mock
    private CoreCaseDataService coreCaseDataService;

    private UpdateCoreCaseDataService updateCoreCaseDataService;

    @Before
    public void setUp() {
        updateCoreCaseDataService = new UpdateCoreCaseDataService(coreCaseDataApi, coreCaseDataService);
    }

    @Test
    public void givenACase_shouldBeUpdatedInCcd() {
        //Given
        mockStartEventResponse();
        mockCaseDetails();
        when(coreCaseDataService.getEventRequestData(anyString()))
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
        CaseDetails caseDetails = updateCoreCaseDataService.updateCase(
            CaseDataUtils.buildCaseData("SC068/17/00013"), 1L, "responseReceived");

        //Then
        assertNotNull(caseDetails);
        CaseData caseData = (CaseData) caseDetails.getData().get("caseReference");
        assertEquals("SC068/17/00013", caseData.getCaseReference());
    }

    private void mockCaseDetails() {
        Map<String, Object> caseData = new HashMap<>(1);
        caseData.put("caseReference", CaseDataUtils.buildCaseData("SC068/17/00013"));
        CaseDetails caseDetails = CaseDetails.builder().data(caseData).build();
        when(coreCaseDataApi.submitEventForCaseWorker(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyBoolean(),
            any(CaseDataContent.class))
        ).thenReturn(caseDetails);
    }

    private void mockStartEventResponse() {
        when(coreCaseDataApi.startEventForCaseWorker(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString())
        ).thenReturn(
            StartEventResponse.builder()
                .caseDetails(CaseDetails.builder().build())
                .build());
    }
}
