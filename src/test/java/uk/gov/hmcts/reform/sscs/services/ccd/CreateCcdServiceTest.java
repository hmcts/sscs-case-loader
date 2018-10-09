package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.sscs.ccd.config.CcdRequestDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;

@RunWith(MockitoJUnitRunner.class)
public class CreateCcdServiceTest {

    private static final String OAUTH2 = "token";
    private static final String SERVICE_AUTHORIZATION = "auth";
    private static final String USER_ID = "16";
    private static final String EVENT_ID = "appealCreated";
    private static final String CCD_TOKEN = "ccdToken";
    private static final String CCD_EVENT = "ccdEvent";

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
    @Mock
    private SearchCcdService searchCcdService;

    private CcdRequestDetails ccdRequestDetails;
    private SscsCaseData caseData;
    private IdamTokens idamTokens;

    private CreateCcdService createCcdService;

    @Before
    public void setUp() {
        ccdRequestDetails = CcdRequestDetails.builder().jurisdictionId("SSCS").caseTypeId("Benefits").build();

        idamTokens = IdamTokens.builder()
            .idamOauth2Token(OAUTH2)
            .serviceAuthorization(SERVICE_AUTHORIZATION)
            .userId(USER_ID)
            .build();

        when(startEventCcdService.startCase(idamTokens, EVENT_ID))
            .thenReturn(response);

        when(response.getToken()).thenReturn(CCD_TOKEN);
        when(response.getEventId()).thenReturn(CCD_EVENT);

        caseData = SscsCaseData.builder().build();

        createCcdService = new CreateCcdService(ccdRequestDetails, ccdApi, idamService, startEventCcdService,
            searchCcdService);
    }

    @Test
    public void shouldCallCcdCreateMethodsGivenNewCase() {

        ArgumentCaptor<CaseDataContent> captor = ArgumentCaptor.forClass(CaseDataContent.class);

        when(ccdApi.submitForCaseworker(
            eq(OAUTH2),
            eq(SERVICE_AUTHORIZATION),
            eq(USER_ID),
            eq(ccdRequestDetails.getJurisdictionId()),
            eq(ccdRequestDetails.getCaseTypeId()),
            eq(true),
            captor.capture())).thenReturn(caseDetails);

        CaseDetails actual = createCcdService.create(caseData, idamTokens);

        CaseDataContent content = captor.getValue();
        assertThat(content.getEvent().getSummary(), is("GAPS2 Case"));
        assertThat(content.getData(), is(caseData));

        assertThat(actual, is(caseDetails));
    }

}
