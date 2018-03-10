package uk.gov.hmcts.reform.sscs.services;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableMap;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.sscs.models.idam.Authorize;
import uk.gov.hmcts.reform.sscs.services.idam.IdamApiClient;

@RunWith(SpringRunner.class)
@SpringBootTest
public abstract class MockCcdIdamServices {

    @MockBean
    AuthTokenGenerator authTokenGenerator;
    @MockBean
    CoreCaseDataApi coreCaseDataApi;
    @MockBean
    IdamApiClient idamApiClient;
    @MockBean
    private JSch jsch;

    @Before
    public void setUp() {

        Map<String, Object> caseDataMap = new HashMap<>(1);
        Map<String, Object> evidenceMap = new LinkedHashMap<>();
        evidenceMap.put("documents", new ArrayList<HashMap<String, Object>>());
        caseDataMap.put("evidence", evidenceMap);

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

    }

    @SuppressWarnings("unchecked")
    void mockSftp() throws JSchException, SftpException {
        Session session = mock(Session.class);
        given(jsch.getSession(
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
