package uk.gov.hmcts.reform.sscs.services;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableMap;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.sscs.models.idam.Authorize;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.services.ccd.CreateCoreCaseDataService;
import uk.gov.hmcts.reform.sscs.services.ccd.UpdateCoreCaseDataService;
import uk.gov.hmcts.reform.sscs.services.idam.IdamApiClient;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration
@TestPropertySource(properties = { "sscs.case.loader.ignoreCasesBeforeDate=2017-01-01" })
public class ProcessCaseTest {

    private static final int EXPECTED_NUMBER_OF_CASES_TO_CREATE_IN_CCD = 1;
    private static final int EXPECTED_NUMBER_OF_CASES_TO_UPDATE_IN_CCD = 16;

    @MockBean
    private JSch jschSshChannel;
    @MockBean
    private CoreCaseDataApi coreCaseDataApi;
    @MockBean
    private AuthTokenGenerator authTokenGenerator;
    @MockBean
    private IdamApiClient idamApiClient;
    @SpyBean
    private CreateCoreCaseDataService createCoreCaseDataService;
    @SpyBean
    private UpdateCoreCaseDataService updateCoreCaseDataService;

    @Autowired
    private CaseLoaderService caseLoaderService;

    @Test
    public void shouldBeSavedIntoCcdGivenDeltaXmlInSftp() throws JSchException, SftpException, IOException {
        mockSftp();

        given(authTokenGenerator.generate()).willReturn("s2s token");

        given(coreCaseDataApi.startForCaseworker(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString())
        ).willReturn(StartEventResponse.builder().build());

        Map<String, Object> caseDataMap = new HashMap<>(1);
        Map<String, Object> evidenceMap = new LinkedHashMap<>();
        evidenceMap.put("documents", new ArrayList<HashMap<String, Object>>());
        caseDataMap.put("evidence", evidenceMap);

        given(coreCaseDataApi.submitForCaseworker(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            eq(Boolean.TRUE),
            any(CaseDataContent.class)
        )).willReturn(CaseDetails.builder().data(caseDataMap).build());

        given(idamApiClient.authorizeCodeType(
            anyString(),
            anyString(),
            anyString(),
            anyString())
        ).willReturn(new Authorize("url", "code", ""));

        given(idamApiClient.authorizeToken(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString())
        ).willReturn(new Authorize("", "", "accessToken"));

        given(coreCaseDataApi.searchForCaseworker(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            any())
        ).willReturn(Collections.singletonList(CaseDetails.builder().id(1L).data(caseDataMap).build()));

        // Override for 1 create case
        given(coreCaseDataApi.searchForCaseworker(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            eq(ImmutableMap.of("case.caseReference", "SC068/17/00004")))
        ).willReturn(new ArrayList<>());

        given(coreCaseDataApi.startEventForCaseWorker(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString()
        )).willReturn(StartEventResponse.builder().build());

        given(coreCaseDataApi.submitEventForCaseWorker(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyBoolean(),
            any(CaseDataContent.class)
        )).willReturn(CaseDetails.builder().build());

        caseLoaderService.process();

        verify(createCoreCaseDataService, times(EXPECTED_NUMBER_OF_CASES_TO_CREATE_IN_CCD))
            .createCcdCase(any(CaseData.class));

        verify(coreCaseDataApi, times(EXPECTED_NUMBER_OF_CASES_TO_CREATE_IN_CCD))
            .submitForCaseworker(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                eq(Boolean.TRUE),
                any(CaseDataContent.class)
            );

        verify(updateCoreCaseDataService, times(EXPECTED_NUMBER_OF_CASES_TO_UPDATE_IN_CCD))
            .updateCase(any(CaseData.class), anyLong(), anyString());

        verify(coreCaseDataApi, times(EXPECTED_NUMBER_OF_CASES_TO_UPDATE_IN_CCD))
            .submitEventForCaseWorker(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                eq(Boolean.TRUE),
                any(CaseDataContent.class)
            );
    }

    private void mockSftp() throws JSchException, SftpException, IOException {
        Session session = mock(Session.class);
        given(jschSshChannel.getSession(
            anyString(),
            anyString(),
            anyInt())
        ).willReturn(session);

        String deltaFileName = "SSCS_Extract_Delta_2017-05-24-16-14-19.xml";
        InputStream deltaStream = getClass().getClassLoader().getResourceAsStream(deltaFileName);

        ChannelSftp channelSftp = mock(ChannelSftp.class);
        given(session.openChannel(anyString())).willReturn(channelSftp);
        given(channelSftp.get(anyString())).willReturn(deltaStream);

        ChannelSftp.LsEntry file = mock(ChannelSftp.LsEntry.class);
        given(file.getFilename()).willReturn(deltaFileName);

        List<ChannelSftp.LsEntry> fileList = newArrayList();
        fileList.add(file);

        given(channelSftp.ls(anyString())).willReturn(new Vector(fileList));
    }

}
