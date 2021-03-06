package uk.gov.hmcts.reform.sscs.services;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKey.BAT_CODE_MAP;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKey.BEN_ASSESS_TYPE;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKey.CASE_CODE;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKeyField.BAT_CODE;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKeyField.BENEFIT_DESC;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKeyField.BEN_ASSESS_TYPE_ID;

import java.util.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.ccd.service.SscsQueryBuilder;
import uk.gov.hmcts.reform.sscs.refdata.RefDataRepository;
import uk.gov.hmcts.reform.sscs.services.gaps2.files.Gaps2File;
import uk.gov.hmcts.reform.sscs.services.refdata.ReferenceDataService;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpChannelAdapter;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ProcessCaseTest {

    private static final String USER_AUTH =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1"
            + "NiJ9.eyJzdWIiOiIxNiIsIm5hbWUiOiJ"
            + "UZXN0IiwianRpIjoiMTIzNCIsImlhdCI"
            + "6MTUyNjkyOTk1MiwiZXhwIjoxNTI2OTM"
            + "zNTg5fQ.lZwrWNjG-y1Olo1qWocKIuq3"
            + "_fdffVF8BTcR5l87FTg";

    private static final String USER_AUTH_WITH_TYPE = "Bearer " + USER_AUTH;

    private static final String SERVER_AUTH =
        "Bearer "
            + "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ"
            + "zc2NzIiwiZXhwIjoxNTI2NjU2NTEyfQ."
            + "aADJFE6_FJPNpDO_0NbqS-oYIDM9Bjjh"
            + "18ZyB1imXGXAqOEc8Iyy0zxBe6BhXFl8"
            + "E8panNAv3zdDDeOhlrEViQ";

    private static final String USER_ID = "16";

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @MockBean
    private IdamClient idamClient;

    @MockBean
    private SftpChannelAdapter channelAdapter;

    @MockBean
    private RefDataRepository refDataRepository;

    @Autowired
    private ReferenceDataService referenceDataService;

    @Autowired
    private CaseLoaderService caseLoaderService;

    @Before
    public void setUp() {

        Map<String, Object> caseDataMap = new HashMap<>(1);
        Map<String, Object> evidenceMap = new LinkedHashMap<>();
        evidenceMap.put("documents", new ArrayList<HashMap<String, Object>>());
        caseDataMap.put("evidence", evidenceMap);
        caseDataMap.put("appeal", buildAppeal());

        String refFilename = "SSCS_Extract_Reference_2017-05-24-16-14-19.xml";
        String deltaFilename = "SSCS_Extract_Delta_2018-05-01-01-01-01.xml";

        when(channelAdapter.listFailed()).thenReturn(newArrayList());
        when(channelAdapter.listProcessed()).thenReturn(newArrayList());
        when(channelAdapter.listIncoming())
            .thenReturn(newArrayList(new Gaps2File(refFilename, 10L), new Gaps2File(deltaFilename, 10L)));

        when(channelAdapter.getInputStream(refFilename)).thenAnswer(x ->
            getClass().getClassLoader().getResourceAsStream("SSCS_Extract_Reference_2017-05-24-16-14-19.xml"));

        when(channelAdapter.getInputStream(deltaFilename)).thenAnswer(x ->
            getClass().getClassLoader().getResourceAsStream("process_case_test_delta.xml"));

        given(authTokenGenerator.generate()).willReturn(SERVER_AUTH);

        when(idamClient.getAccessToken(anyString(), anyString())).thenReturn("Bearer " + USER_AUTH);

        when(idamClient.getUserInfo(anyString()))
            .thenReturn(new uk.gov.hmcts.reform.idam.client.models.UserInfo("sub", "16", "m@test.com", "test", "test",
                new ArrayList<>()));

        when(refDataRepository.find(CASE_CODE, "1001", BEN_ASSESS_TYPE_ID)).thenReturn("bat");
        when(refDataRepository.find(BEN_ASSESS_TYPE, "bat", BAT_CODE)).thenReturn("code");
        when(refDataRepository.find(BAT_CODE_MAP, "code", BENEFIT_DESC)).thenReturn("PIP");

        referenceDataService.setRefDataRepo(refDataRepository);

        // SC reference case

        CaseDetails scReferenceCaseDetails =
            CaseDetails.builder()
                .id(123L)
                .data(caseDataMap)
                .build();

        SearchSourceBuilder query = SscsQueryBuilder.findCaseBySingleField("data.caseReference", "SC068/01/00001");

        when(coreCaseDataApi.searchCases(
            anyString(),
            anyString(),
            anyString(),
            eq(query.toString())
        )).thenReturn(SearchResult.builder().cases(new ArrayList<>()).build());

        when(coreCaseDataApi.startForCaseworker(
            anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(StartEventResponse.builder().build());

        when(coreCaseDataApi.submitForCaseworker(
            anyString(), anyString(), anyString(), anyString(), anyString(),
            eq(Boolean.TRUE), any(CaseDataContent.class)))
            .thenReturn(scReferenceCaseDetails);

        // CCD ID case

        CaseDetails ccdCaseDetails =
            CaseDetails.builder()
                .id(456L)
                .data(caseDataMap)
                .build();

        when(coreCaseDataApi.readForCaseWorker(
            anyString(), anyString(), anyString(), anyString(), anyString(),
            eq("1234567890")))
            .thenReturn(ccdCaseDetails);

        when(coreCaseDataApi.startEventForCaseWorker(
            anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(StartEventResponse.builder().build());

        when(coreCaseDataApi.submitEventForCaseWorker(
            anyString(), anyString(), anyString(), anyString(), anyString(), anyString(),
            anyBoolean(), any(CaseDataContent.class)))
            .thenReturn(ccdCaseDetails);

    }

    @Test
    public void shouldBeSavedIntoCcdGivenDeltaXmlInSftp() {

        caseLoaderService.process();

        // SC reference case

        SearchSourceBuilder query = SscsQueryBuilder.findCaseBySingleField("data.caseReference", "SC068/01/00001");

        verify(coreCaseDataApi, times(1)).searchCases(
            eq(USER_AUTH_WITH_TYPE),
            eq(SERVER_AUTH),
            anyString(),
            eq(query.toString())
        );

        // CCD ID case

        verify(coreCaseDataApi, times(1)).readForCaseWorker(
            eq(USER_AUTH_WITH_TYPE),
            eq(SERVER_AUTH),
            eq(USER_ID),
            anyString(),
            anyString(),
            eq("1234567890")
        );

        ArgumentCaptor<CaseDataContent> capture = ArgumentCaptor.forClass(CaseDataContent.class);

        verify(coreCaseDataApi, times(1)).submitEventForCaseWorker(
            eq(USER_AUTH_WITH_TYPE),
            eq(SERVER_AUTH),
            eq(USER_ID),
            eq("SSCS"),
            eq("Benefit"),
            eq("456"),
            eq(true),
            capture.capture()
        );

        assertEquals("Liverpool", ((SscsCaseData) capture.getValue().getData()).getProcessingVenue());
    }

    private Appeal buildAppeal() {
        Name name = Name.builder()
            .title("Mr")
            .firstName("User")
            .lastName("Test")
            .build();
        Contact contact = Contact.builder()
            .email("mail@email.com")
            .phone("01234567890")
            .mobile("01234567890")
            .build();
        Identity identity = Identity.builder()
            .dob("1904-03-10")
            .nino("AB 22 55 66 B")
            .build();
        Appellant appellant = Appellant.builder()
            .name(name)
            .contact(contact)
            .identity(identity)
            .build();
        BenefitType benefitType = BenefitType.builder()
            .code("PIP")
            .build();

        return Appeal.builder()
            .appellant(appellant)
            .benefitType(benefitType)
            .build();
    }
}
