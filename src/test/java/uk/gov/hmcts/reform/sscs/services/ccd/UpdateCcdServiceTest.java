package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.sscs.config.properties.CoreCaseDataProperties;
import uk.gov.hmcts.reform.sscs.models.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.services.idam.IdamService;

@RunWith(MockitoJUnitRunner.class)
public class UpdateCcdServiceTest {

    private static final String OAUTH2 = "token";
    private static final String SERVICE_AUTHORIZATION = "auth";
    private static final String SERVICE_USER_ID = "sscs";
    private static final String EVENT_ID = "appealReceived";
    private static final String CCD_TOKEN = "ccdToken";
    private static final String CCD_EVENT = "ccdEvent";
    private static final Long CASE_ID = 1L;

    @Mock
    private IdamService idamService;
    @Mock
    private CoreCaseDataApi ccdApi;
    @Mock
    private StartEventResponse response;
    @Mock
    private CaseDetails caseDetails;
    @Mock
    private StartEventCcdService startEventCcdService;

    private CoreCaseDataProperties ccdProperties;
    private CaseData caseData;
    private IdamTokens idamTokens;

    private UpdateCcdService updateCcdService;

    @Before
    public void setUp() {
        stub(idamService.generateServiceAuthorization()).toReturn(SERVICE_AUTHORIZATION);
        stub(idamService.getIdamOauth2Token()).toReturn(OAUTH2);

        ccdProperties = new CoreCaseDataProperties();
        ccdProperties.setJurisdictionId("SSCS");
        ccdProperties.setCaseTypeId("Benefits");

        idamTokens = IdamTokens.builder()
            .idamOauth2Token(OAUTH2)
            .authenticationService(SERVICE_AUTHORIZATION)
            .serviceUserId(SERVICE_USER_ID)
            .build();

        when(startEventCcdService.startEvent(idamTokens, CASE_ID.toString(), EVENT_ID))
            .thenReturn(response);

        when(response.getToken()).thenReturn(CCD_TOKEN);
        when(response.getEventId()).thenReturn(CCD_EVENT);

        caseData = CaseData.builder().build();

        updateCcdService = new UpdateCcdService(ccdProperties, ccdApi, idamService, startEventCcdService);
    }

    @Test
    public void shouldCallCcdUpdateMethodsGivenUpdatedCase() {

        ArgumentCaptor<CaseDataContent> captor = ArgumentCaptor.forClass(CaseDataContent.class);

        when(ccdApi.submitEventForCaseWorker(
            eq(OAUTH2),
            eq(SERVICE_AUTHORIZATION),
            eq(SERVICE_USER_ID),
            eq(ccdProperties.getJurisdictionId()),
            eq(ccdProperties.getCaseTypeId()),
            eq(CASE_ID.toString()),
            eq(true),
            captor.capture())).thenReturn(caseDetails);

        CaseDetails actual = updateCcdService.update(caseData, CASE_ID, EVENT_ID, idamTokens);

        CaseDataContent content = captor.getValue();
        assertThat(content.getEvent().getSummary(), is("GAPS2 Case"));
        assertThat(content.getData(), is(caseData));

        assertThat(actual, is(caseDetails));
    }

}
