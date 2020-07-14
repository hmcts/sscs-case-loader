package uk.gov.hmcts.reform.sscs.services;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import javax.xml.stream.XMLStreamException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.BenefitType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.SearchCcdCaseService;
import uk.gov.hmcts.reform.sscs.exceptions.ProcessDeltaException;
import uk.gov.hmcts.reform.sscs.exceptions.TransformException;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.service.RefDataFactory;
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
    private IdamTokens idamTokens;

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
        idamTokens = IdamTokens.builder()
            .idamOauth2Token("idamOauth2Token")
            .serviceAuthorization("serviceAuthorization")
            .userId("16")
            .build();
        when(idamService.getIdamTokens()).thenReturn(idamTokens);
    }

    @Test
    public void shouldLoadCasesGivenIncomingXmlFiles() {
        when(transformService.transform(inputStream)).thenReturn(newArrayList(caseData));

        caseLoaderService.process();

        verify(xmlValidator, times(2)).validateXml(file);
        verify(sftpSshService, times(2)).move(file, true);
    }

    @Test
    public void shouldUpdateCaseUsingCaseRefGivenIncomingXmlFiles() {
        final SscsCaseDetails sscsCaseDetails = SscsCaseDetails.builder().build();

        when(transformService.transform(inputStream)).thenReturn(newArrayList(caseData));

        when(searchCcdCaseService.findCaseByCaseRefOrCaseId(eq(caseData), eq(idamTokens)))
            .thenReturn(sscsCaseDetails);

        caseLoaderService.process();

        verify(xmlValidator, times(2)).validateXml(file);
        verify(ccdCasesSender, times(1)).sendUpdateCcdCases(caseData, sscsCaseDetails, idamTokens);
        verify(sftpSshService, times(2)).move(file, true);
    }

    @Test
    public void shouldUpdateValidCasesWhenMixedWithInvalidCases() {

        final SscsCaseDetails sscsCaseDetails = SscsCaseDetails.builder().build();

        when(searchCcdCaseService.findCaseByCaseRefOrCaseId(eq(caseData), eq(idamTokens)))
            .thenThrow(new NumberFormatException())
            .thenReturn(sscsCaseDetails);

        when(transformService.transform(inputStream)).thenReturn(newArrayList(caseData));

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

        when(searchCcdCaseService.findCaseByCaseRefOrCaseId(eq(caseData), eq(idamTokens)))
            .thenReturn(sscsCaseDetails);

        when(transformService.transform(inputStream)).thenReturn(newArrayList(caseData));

        caseLoaderService.process();

        verify(xmlValidator, times(2)).validateXml(file);
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
    public void shouldThrowExceptionGivenNoReferenceFileLoaded() {
        when(file.isDelta()).thenReturn(true);
        caseLoaderService.process();
    }

    @Test(expected = ProcessDeltaException.class)
    public void shouldThrowExceptionWhileUpdatingCase() {
        final SscsCaseDetails sscsCaseDetails = SscsCaseDetails.builder().build();
        caseData.setCaseReference(null);
        caseData.setCcdCaseId("1234567890");

        when(transformService.transform(inputStream)).thenReturn(newArrayList(caseData));

        when(searchCcdCaseService.findCaseByCaseRefOrCaseId(eq(caseData), eq(idamTokens)))
                .thenReturn(sscsCaseDetails);

        doThrow(RuntimeException.class).when(ccdCasesSender).sendUpdateCcdCases(any(), any(), any());

        caseLoaderService.process();

    }

}
