package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
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
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.services.idam.IdamService;

@RunWith(MockitoJUnitRunner.class)
public class CcdApiWrapperTest {

    private static final String OAUTH2 = "token";
    private static final String S2SAUTH = "auth";
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

    private CoreCaseDataProperties ccdProperties;
    private CaseData caseData;

    private CcdApiWrapper apiWrapper;

    @Before
    public void setUp() {
        stub(idamService.generateServiceAuthorization()).toReturn(S2SAUTH);
        stub(idamService.getIdamOauth2Token()).toReturn(OAUTH2);

        ccdProperties = new CoreCaseDataProperties();
        ccdProperties.setUserId("USER");
        ccdProperties.setJurisdictionId("SSCS");
        ccdProperties.setCaseTypeId("Benefits");

        when(ccdApi.startForCaseworker(OAUTH2,
            S2SAUTH,
            ccdProperties.getUserId(),
            ccdProperties.getJurisdictionId(),
            ccdProperties.getCaseTypeId(),
            EVENT_ID)).thenReturn(response);

        when(response.getToken()).thenReturn(CCD_TOKEN);
        when(response.getEventId()).thenReturn(CCD_EVENT);

        caseData = CaseData.builder().build();

        apiWrapper = new CcdApiWrapper(ccdProperties, ccdApi, idamService);
    }

    @Test
    public void shouldCallCcdCreateMethodsGivenNewCase() {

        ArgumentCaptor<CaseDataContent> captor = ArgumentCaptor.forClass(CaseDataContent.class);

        when(ccdApi.submitForCaseworker(eq(OAUTH2),
            eq(S2SAUTH),
            eq(ccdProperties.getUserId()),
            eq(ccdProperties.getJurisdictionId()),
            eq(ccdProperties.getCaseTypeId()),
            eq(true),
            captor.capture())).thenReturn(caseDetails);

        CaseDetails actual = apiWrapper.create(caseData, OAUTH2, S2SAUTH);

        CaseDataContent content = captor.getValue();
        assertThat(content.getEvent().getSummary(), is("GAPS2 Case"));
        assertThat(content.getData(), is(caseData));

        assertThat(actual, is(caseDetails));
    }

    @Test
    public void shouldCallCcdUpdateMethodsGivenUpdatedCase() {

        when(ccdApi.submitEventForCaseWorker(eq(OAUTH2),
            eq(S2SAUTH),
            eq(ccdProperties.getUserId()),
            eq(ccdProperties.getJurisdictionId()),
            eq(ccdProperties.getCaseTypeId()),
            eq("123"),
            eq(true),
            any(CaseDataContent.class))).thenReturn(caseDetails);

        assertThat(apiWrapper.update(caseData, 123L, EVENT_ID, OAUTH2, S2SAUTH), is(caseDetails));
    }
}
