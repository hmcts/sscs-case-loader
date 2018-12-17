package uk.gov.hmcts.reform.sscs.services;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.BenefitType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.SearchCcdCaseService;
import uk.gov.hmcts.reform.sscs.exceptions.TransformException;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.refdata.RefDataFactory;
import uk.gov.hmcts.reform.sscs.services.ccd.CcdCasesSender;
import uk.gov.hmcts.reform.sscs.services.gaps2.files.Gaps2File;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpSshService;
import uk.gov.hmcts.reform.sscs.services.xml.XmlValidator;

@RunWith(MockitoJUnitRunner.class)
public class CaseLoaderServiceTest {

    @Mock
    private SftpSshService sftpSshService;
    @Mock
    private XmlValidator xmlValidator;
    @Mock
    private TransformationService transformService;
    @Mock
    private CcdCasesSender ccdCasesSender;
    @Mock
    private RefDataFactory refDataFactory;
    @Mock
    private Gaps2File file;
    @Mock
    private InputStream inputStream;
    @Mock
    private SearchCcdCaseService searchCcdCaseService;
    @Mock
    private IdamService idamService;

    private SscsCaseData caseData;

    private CaseLoaderService caseLoaderService;

    @Before
    public void setUp() {
        when(sftpSshService.getFiles()).thenReturn(newArrayList(file, file));
        when(sftpSshService.readExtractFile(file)).thenReturn(inputStream);
        when(file.isDelta()).thenReturn(false).thenReturn(true);

        caseLoaderService = new CaseLoaderService(sftpSshService,
            xmlValidator,
            transformService,
            ccdCasesSender,
            refDataFactory,
            idamService,
            searchCcdCaseService);

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
    public void shouldLoadCasesGivenIncomingXmlFiles() {
        when(transformService.transform(inputStream)).thenReturn(newArrayList(caseData));

        caseLoaderService.process();

        verify(xmlValidator, times(2)).validateXml(file);
        verify(sftpSshService, times(2)).move(file, true);
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

    @Test
    public void shouldUpdateCaseUsingCaseRefGivenIncomingXmlFiles() {

        final SscsCaseDetails sscsCaseDetails = SscsCaseDetails.builder().build();

        IdamTokens idamTokens = IdamTokens.builder()
            .idamOauth2Token("idamOauth2Token")
            .serviceAuthorization("serviceAuthorization")
            .userId("16")
            .build();

        when(searchCcdCaseService.findCaseByCaseRefOrCaseId(eq(caseData), eq(idamTokens)))
            .thenReturn(sscsCaseDetails);

        when(transformService.transform(inputStream)).thenReturn(newArrayList(caseData));
        when(idamService.getIdamOauth2Token()).thenReturn("idamOauth2Token");
        when(idamService.generateServiceAuthorization()).thenReturn("serviceAuthorization");
        when(idamService.getUserId("idamOauth2Token")).thenReturn("16");

        caseLoaderService.process();

        verify(xmlValidator, times(2)).validateXml(file);
        verify(ccdCasesSender, times(1)).sendUpdateCcdCases(caseData, sscsCaseDetails, idamTokens);
        verify(sftpSshService, times(2)).move(file, true);
        verify(searchCcdCaseService, times(1))
            .findCaseByCaseRefOrCaseId(eq(caseData), eq(idamTokens));
        verify(ccdCasesSender, times(1)).sendUpdateCcdCases(caseData, sscsCaseDetails, idamTokens);
        verify(sftpSshService, times(2)).move(file, true);
    }

    @Test
    public void shouldUpdateValidCasesWhenMixedWithInvalidCases() {

        final SscsCaseDetails sscsCaseDetails = SscsCaseDetails.builder().build();

        IdamTokens idamTokens = IdamTokens.builder()
            .idamOauth2Token("idamOauth2Token")
            .serviceAuthorization("serviceAuthorization")
            .userId("16")
            .build();

        when(searchCcdCaseService.findCaseByCaseRefOrCaseId(eq(caseData), eq(idamTokens)))
            .thenThrow(new NumberFormatException())
            .thenReturn(sscsCaseDetails);

        when(transformService.transform(inputStream)).thenReturn(newArrayList(caseData));
        when(idamService.getIdamOauth2Token()).thenReturn("idamOauth2Token");
        when(idamService.generateServiceAuthorization()).thenReturn("serviceAuthorization");
        when(idamService.getUserId("idamOauth2Token")).thenReturn("16");

        caseLoaderService.process();

        verify(xmlValidator, times(2)).validateXml(file);
        verify(sftpSshService, times(2)).move(file, true);
        verify(searchCcdCaseService, times(1))
            .findCaseByCaseRefOrCaseId(eq(caseData), eq(idamTokens));
        verify(sftpSshService, times(2)).move(file, true);
    }

    @Test
    public void shouldUpdateCaseUsingCaseCcdIdGivenIncomingXmlFiles() {

        final SscsCaseDetails sscsCaseDetails = SscsCaseDetails.builder().build();
        caseData.setCaseReference(null);
        caseData.setCcdCaseId("1234567890");

        IdamTokens idamTokens = IdamTokens.builder()
            .idamOauth2Token("idamOauth2Token")
            .serviceAuthorization("serviceAuthorization")
            .userId("16")
            .build();

        when(searchCcdCaseService.findCaseByCaseRefOrCaseId(eq(caseData), eq(idamTokens)))
            .thenReturn(sscsCaseDetails);

        when(transformService.transform(inputStream)).thenReturn(newArrayList(caseData));
        when(idamService.getIdamOauth2Token()).thenReturn("idamOauth2Token");
        when(idamService.generateServiceAuthorization()).thenReturn("serviceAuthorization");
        when(idamService.getUserId("idamOauth2Token")).thenReturn("16");

        caseLoaderService.process();

        verify(xmlValidator, times(2)).validateXml(file);
        verify(ccdCasesSender, times(1)).sendUpdateCcdCases(caseData, sscsCaseDetails, idamTokens);
        verify(sftpSshService, times(2)).move(file, true);
        verify(searchCcdCaseService, times(1))
            .findCaseByCaseRefOrCaseId(eq(caseData), eq(idamTokens));
        verify(ccdCasesSender, times(1)).sendUpdateCcdCases(caseData, sscsCaseDetails, idamTokens);
        verify(sftpSshService, times(2)).move(file, true);

        verify(searchCcdCaseService, never()).findCaseByCaseRef(any(), any());
    }

    @Test
    public void shouldProcessReferenceDataGivenFile() throws XMLStreamException {
        caseLoaderService.process();

        verify(xmlValidator, times(2)).validateXml(file);
        verify(refDataFactory).extract(inputStream);
        verify(sftpSshService, times(2)).move(file, true);
    }

    @Test(expected = TransformException.class)
    public void shouldThrowExceptionGivenReferenceExtractFails() throws XMLStreamException {
        when(file.isDelta()).thenReturn(false);

        doThrow(new XMLStreamException()).when(refDataFactory).extract(inputStream);

        caseLoaderService.process();

        verify(xmlValidator).validateXml(file);
    }

    @Test(expected = TransformException.class)
    public void shouldThrowExceptionGivenNoReferenceFileLoaded() throws XMLStreamException {
        when(file.isDelta()).thenReturn(true);
        caseLoaderService.process();
    }
}
