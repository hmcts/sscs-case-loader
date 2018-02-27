package uk.gov.hmcts.reform.sscs.services;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.io.File;
import java.util.Collections;
import java.util.Vector;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.sscs.models.idam.Authorize;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.services.ccd.CreateCoreCaseDataService;
import uk.gov.hmcts.reform.sscs.services.idam.IdamApiClient;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CaseLoaderServiceTest {

    private static final int EXPECTED_NUMBER_OF_CASES_TO_SEND_TO_CCD = 2;
    private static final String DELTA_XML = "src/test/resources/SSCS_Extract_Delta_2017-05-24-16-14-19.xml";

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

    @Autowired
    private CaseLoaderService caseLoaderService;


    @Test
    public void givenDeltaXmlInSftp_shouldBeSavedIntoCcd() throws Exception {
        Session session = mock(Session.class);
        given(jschSshChannel.getSession(
            anyString(),
            anyString(),
            anyInt())
        ).willReturn(session);

        ChannelSftp channelSftp = mock(ChannelSftp.class);
        given(session.openChannel(anyString())).willReturn(channelSftp);
        given(channelSftp.get(anyString())).willReturn(FileUtils.openInputStream(new File(DELTA_XML)));

        ChannelSftp.LsEntry file = mock(ChannelSftp.LsEntry.class);
        given(file.getFilename()).willReturn("SSCS_Extract_Delta");

        Vector<ChannelSftp.LsEntry> fileList = new Vector<>();
        fileList.add(file);

        given(channelSftp.ls(anyString())).willReturn(fileList);

        given(authTokenGenerator.generate()).willReturn("s2s token");

        given(coreCaseDataApi.startForCaseworker(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString())
        ).willReturn(StartEventResponse.builder().build());

        given(coreCaseDataApi.submitForCaseworker(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            eq(Boolean.TRUE),
            any(CaseDataContent.class)
        )).willReturn(CaseDetails.builder().build());

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
        ).willReturn(Collections.singletonList(CaseDetails.builder().id(1L).build()));

        given(coreCaseDataApi.startEventForCaseWorker(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString()
        )).willReturn(StartEventResponse.builder().build());

        caseLoaderService.process();

        verify(createCoreCaseDataService, times(EXPECTED_NUMBER_OF_CASES_TO_SEND_TO_CCD))
            .createCcdCase(any(CaseData.class));

        verify(coreCaseDataApi, times(EXPECTED_NUMBER_OF_CASES_TO_SEND_TO_CCD))
            .submitForCaseworker(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                eq(Boolean.TRUE),
                any(CaseDataContent.class)
            );
    }

}
