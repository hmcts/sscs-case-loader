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
import uk.gov.hmcts.reform.sscs.config.properties.IdamProperties;
import uk.gov.hmcts.reform.sscs.models.idam.Authorize;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.services.idam.IdamApiClient;

@RunWith(MockitoJUnitRunner.class)
public class UpdateCoreCaseDataServiceTest {

    @Mock
    private CoreCaseDataApi coreCaseDataApiMock;
    private UpdateCoreCaseDataService updateCoreCaseDataService;
    @Mock
    private CoreCaseDataProperties coreCaseDataPropertiesMock;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private IdamApiClient idamApiClient;
    @Mock
    private IdamProperties idamProperties;

    @Before
    public void setUp() {
        updateCoreCaseDataService = new UpdateCoreCaseDataService(new CoreCaseDataService(coreCaseDataApiMock,
            coreCaseDataPropertiesMock, authTokenGenerator, idamApiClient, idamProperties));
    }

    @Test
    public void givenACase_shouldUpdateItInCcd() {
        //Given
        mockCoreCaseDataProperties();
        mockStartEventResponse();
        mockCaseDetails();
        when(idamApiClient.authorizeCodeType(
            anyString(),
            anyString(),
            anyString(),
            anyString())
        ).thenReturn(new Authorize("url", "code", ""));

        when(idamApiClient.authorizeToken(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString())
        ).thenReturn(new Authorize("", "", "accessToken"));
        mockIdamProrperties();

        //When
        CaseDetails caseDetails = updateCoreCaseDataService.updateCase(CaseDataUtils.buildCaseData(), 2L);

        //Then
        assertNotNull(caseDetails);
        CaseData caseData = (CaseData) caseDetails.getData().get("case-data");
        assertEquals("AB 22 55 66 B", caseData.getAppeal().getAppellant().getIdentity().getNino());
    }

    private void mockIdamProrperties() {
        IdamProperties.Oauth2 oauth2 = mock(IdamProperties.Oauth2.class);
        when(idamProperties.getOauth2()).thenReturn(oauth2);

        IdamProperties.Oauth2.User user = mock(IdamProperties.Oauth2.User.class);
        when(oauth2.getUser()).thenReturn(user);

        IdamProperties.Oauth2.Client client = mock(IdamProperties.Oauth2.Client.class);
        when(oauth2.getClient()).thenReturn(client);

        when(user.getEmail()).thenReturn("email");
        when(user.getPassword()).thenReturn("password");
    }

    private void mockCaseDetails() {
        Map<String, Object> caseData = new HashMap<>(1);
        caseData.put("case-data", CaseDataUtils.buildCaseData());
        CaseDetails caseDetails = CaseDetails.builder().data(caseData).build();
        when(coreCaseDataApiMock.submitEventForCaseWorker(anyString(), anyString(), anyString(), anyString(),
            anyString(), anyString(), eq(true), any(CaseDataContent.class))).thenReturn(caseDetails);
    }

    private void mockStartEventResponse() {
        StartEventResponse startEventResponseMock = mock(StartEventResponse.class);
        when(coreCaseDataApiMock.startEventForCaseWorker(anyString(), anyString(), anyString(), anyString(),
            anyString(), anyString(), anyString())).thenReturn(startEventResponseMock);
    }

    private void mockCoreCaseDataProperties() {
        when(coreCaseDataPropertiesMock.getUserId()).thenReturn("userId");
        when(coreCaseDataPropertiesMock.getJurisdictionId()).thenReturn("jurisdictionId");
        when(coreCaseDataPropertiesMock.getCaseTypeId()).thenReturn("caseTypeId");
        when(coreCaseDataPropertiesMock.getEventId()).thenReturn("eventId");
    }
}
