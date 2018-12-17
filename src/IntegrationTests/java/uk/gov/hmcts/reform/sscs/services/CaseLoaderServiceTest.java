package uk.gov.hmcts.reform.sscs.services;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.BenefitType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.SearchCcdCaseService;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.refdata.RefDataFactory;
import uk.gov.hmcts.reform.sscs.services.ccd.CcdCasesSender;
import uk.gov.hmcts.reform.sscs.services.gaps2.files.Gaps2File;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpChannelAdapter;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpSshService;
import uk.gov.hmcts.reform.sscs.services.xml.XmlValidator;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("development")
public class CaseLoaderServiceTest {

    @MockBean
    private SftpSshService sftpSshService;

    @MockBean
    private SftpChannelAdapter channelAdapter;

    @MockBean
    private XmlValidator xmlValidator;
    @MockBean
    private TransformationService transformService;
    @MockBean
    private CcdCasesSender ccdCasesSender;
    @MockBean
    private RefDataFactory refDataFactory;
    @Mock
    private Gaps2File file;
    @Mock
    private InputStream inputStream;
    @MockBean
    private SearchCcdCaseService searchCcdCaseService;
    @MockBean
    private IdamService idamService;

    private SscsCaseData caseData;

    @Autowired
    private CaseLoaderService caseLoaderService;

    @Before
    public void setUp() {
        when(sftpSshService.getFiles()).thenReturn(newArrayList(file, file));
        when(sftpSshService.readExtractFile(file)).thenReturn(inputStream);
        when(file.isDelta()).thenReturn(false).thenReturn(true);

        Appeal appeal = Appeal.builder()
            .benefitType(BenefitType.builder()
                .code("PIP")
                .build())
            .build();
        caseData = SscsCaseData.builder()
            .caseReference("caseRef")
            .appeal(appeal)
            .build();
    }

    @Test
    public void givenItHasProcessed100Cases_shouldRenewAuthenticationTokens() {
        IdamTokens idamTokens = IdamTokens.builder()
            .idamOauth2Token("oAuth2Token")
            .serviceAuthorization("s2sToken")
            .userId("16")
            .build();

        IdamTokens idamTokens2 = IdamTokens.builder()
            .idamOauth2Token("oAuth2Token2")
            .serviceAuthorization("s2sToken2")
            .userId("16")
            .build();

        IdamTokens idamTokens3 = IdamTokens.builder()
            .idamOauth2Token("oAuth2Token3")
            .serviceAuthorization("s2sToken3")
            .userId("16")
            .build();

        given(idamService.getIdamTokens())
            .willReturn(idamTokens)
            .willReturn(idamTokens2)
            .willReturn(idamTokens3);

        given(transformService.transform(inputStream)).willReturn(buildCaseListWithGivenNumberOfElements(201));

        given(searchCcdCaseService.findCaseByCaseRefOrCaseId(any(SscsCaseData.class), any(IdamTokens.class)))
            .willReturn(null)
            .willReturn(SscsCaseDetails.builder().build());

        caseLoaderService.process();

        then(idamService).should(times(3)).getIdamTokens();

        ArgumentCaptor<IdamTokens> idamTokensArgumentCaptor = ArgumentCaptor.forClass(IdamTokens.class);
        then(searchCcdCaseService).should(times(201))
            .findCaseByCaseRefOrCaseId(eq(caseData), idamTokensArgumentCaptor.capture());
        List<IdamTokens> idamTokensValues = idamTokensArgumentCaptor.getAllValues();

        long actualNumberOfIdamTokens = idamTokensValues.stream()
            .filter(token -> token == idamTokens)
            .count();
        assertThat(actualNumberOfIdamTokens, is(equalTo(100L)));

        long actualNumberOfIdamTokens2 = idamTokensValues.stream()
            .filter(token -> token == idamTokens2)
            .count();
        assertThat(actualNumberOfIdamTokens2, is(equalTo(100L)));

        long actualNumberOfIdamTokens3 = idamTokensValues.stream()
            .filter(token -> token == idamTokens3)
            .count();
        assertThat(actualNumberOfIdamTokens3, is(equalTo(1L)));

        then(xmlValidator).should(times(2)).validateXml(file);
        then(sftpSshService).should(times(2)).move(file, true);
    }

    private List<SscsCaseData> buildCaseListWithGivenNumberOfElements(int elements) {
        List<SscsCaseData> cases = new ArrayList<>(elements);
        int counter = 0;
        while (counter < elements) {
            cases.add(caseData);
            counter++;
        }
        return cases;
    }

}
