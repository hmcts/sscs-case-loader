package uk.gov.hmcts.reform.sscs.services;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.sscs.TestCaseLoaderApp;
import uk.gov.hmcts.reform.sscs.models.GapsInputStream;
import uk.gov.hmcts.reform.sscs.models.idam.Authorize;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.services.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.sscs.services.idam.IdamApiClient;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpSshService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestCaseLoaderApp.class)
public class CaseLoaderServiceTest {

    @MockBean
    private SftpSshService sftpSshService;
    @MockBean
    private CoreCaseDataApi coreCaseDataApi;
    @MockBean
    private AuthTokenGenerator authTokenGenerator;
    @MockBean
    private IdamApiClient idamApiClient;
    @SpyBean
    private CoreCaseDataService coreCaseDataService;

    @Autowired
    private CaseLoaderService caseLoaderService;

    private static final String DELTA_XML = "src/test/resources/SSCS_Extract_Delta_2017-05-24-16-14-19.xml";
    private static final String REFERENCE_XML = "src/test/resources/SSCS_Extract_Reference_2017-05-24-16-14-19.xml";

    @Test
    public void givenDeltaXmlInSftp_shouldBeSavedIntoCcd() throws IOException {
        List<GapsInputStream> inputStreamList = new ArrayList<>(1);
        InputStream stream = FileUtils.openInputStream(new File(DELTA_XML));
        inputStreamList.add(GapsInputStream.builder().inputStream(stream).isDelta(true).isReference(false)
            .build());

        given(sftpSshService.readExtractFiles()).willReturn(inputStreamList);

        given(authTokenGenerator.generate()).willReturn("s2s token");

        given(coreCaseDataApi.startForCaseworker(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString())
        ).willReturn(StartEventResponse.builder().build());

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

        caseLoaderService.process();

        verify(coreCaseDataService).startEventAndSaveGivenCase(any(CaseData.class));
    }

}
