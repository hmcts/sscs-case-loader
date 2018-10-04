package uk.gov.hmcts.reform.sscs.services;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.*;

import java.io.InputStream;
import javax.xml.stream.XMLStreamException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.BenefitType;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.exceptions.TransformException;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.refdata.RefDataFactory;
import uk.gov.hmcts.reform.sscs.services.ccd.CcdCasesSender;
import uk.gov.hmcts.reform.sscs.services.ccd.SearchCcdService;
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
    private InputStream is;
    @Mock
    private SearchCcdService ccdCaseService;
    @Mock
    private IdamService idamService;

    private SscsCaseData caseData;

    private CaseLoaderService caseLoaderService;

    @Before
    public void setUp() {
        when(sftpSshService.getFiles()).thenReturn(newArrayList(file, file));
        when(sftpSshService.readExtractFile(file)).thenReturn(is);
        when(file.isDelta()).thenReturn(false).thenReturn(true);

        caseLoaderService = new CaseLoaderService(sftpSshService,
            xmlValidator,
            transformService,
            ccdCaseService,
            ccdCasesSender,
            refDataFactory,
            idamService);

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
        when(transformService.transform(is)).thenReturn(newArrayList(caseData));

        caseLoaderService.process();

        verify(xmlValidator, times(2)).validateXml(file);
        verify(sftpSshService, times(2)).move(file, true);
    }

    @Test
    public void shouldUpdateCaseUsingCaseRefGivenIncomingXmlFiles() {

        CaseDetails caseDetails = CaseDetails.builder().build();

        IdamTokens idamTokens = IdamTokens.builder()
            .idamOauth2Token("idamOauth2Token")
            .serviceAuthorization("serviceAuthorization")
            .userId("16")
            .build();

        when(ccdCaseService.findCaseByCaseRef(eq("caseRef"), eq(idamTokens)))
            .thenReturn(newArrayList(caseDetails));

        when(transformService.transform(is)).thenReturn(newArrayList(caseData));
        when(idamService.getIdamOauth2Token()).thenReturn("idamOauth2Token");
        when(idamService.generateServiceAuthorization()).thenReturn("serviceAuthorization");
        when(idamService.getUserId("idamOauth2Token")).thenReturn("16");

        caseLoaderService.process();

        verify(xmlValidator, times(2)).validateXml(file);
        verify(ccdCaseService, times(1)).findCaseByCaseRef(eq("caseRef"), eq(idamTokens));
        verify(ccdCasesSender, times(1)).sendUpdateCcdCases(caseData, caseDetails, idamTokens);
        verify(sftpSshService, times(2)).move(file, true);

        verify(ccdCaseService, never()).findCaseByCaseId(any(), any());
    }

    @Test
    public void shouldUpdateCaseUsingCaseCcdIdGivenIncomingXmlFiles() {

        CaseDetails caseDetails = CaseDetails.builder().build();
        caseData.setCaseReference(null);
        caseData.setCcdCaseId("1234567890");

        IdamTokens idamTokens = IdamTokens.builder()
            .idamOauth2Token("idamOauth2Token")
            .serviceAuthorization("serviceAuthorization")
            .userId("16")
            .build();

        when(ccdCaseService.findCaseByCaseId(eq("1234567890"), eq(idamTokens)))
            .thenReturn(newArrayList(caseDetails));

        when(transformService.transform(is)).thenReturn(newArrayList(caseData));
        when(idamService.getIdamOauth2Token()).thenReturn("idamOauth2Token");
        when(idamService.generateServiceAuthorization()).thenReturn("serviceAuthorization");
        when(idamService.getUserId("idamOauth2Token")).thenReturn("16");

        caseLoaderService.process();

        verify(xmlValidator, times(2)).validateXml(file);
        verify(ccdCaseService, times(1)).findCaseByCaseId(eq("1234567890"), eq(idamTokens));
        verify(ccdCasesSender, times(1)).sendUpdateCcdCases(caseData, caseDetails, idamTokens);
        verify(sftpSshService, times(2)).move(file, true);

        verify(ccdCaseService, never()).findCaseByCaseRef(any(), any());
    }

    @Test
    public void shouldProcessReferenceDataGivenFile() throws XMLStreamException {
        caseLoaderService.process();

        verify(xmlValidator, times(2)).validateXml(file);
        verify(refDataFactory).extract(is);
        verify(sftpSshService, times(2)).move(file, true);
    }

    @Test(expected = TransformException.class)
    public void shouldThrowExceptionGivenReferenceExtractFails() throws XMLStreamException {
        when(file.isDelta()).thenReturn(false);

        doThrow(new XMLStreamException()).when(refDataFactory).extract(is);

        caseLoaderService.process();

        verify(xmlValidator).validateXml(file);
    }

    @Test(expected = TransformException.class)
    public void shouldThrowExceptionGivenNoReferenceFileLoaded() throws XMLStreamException {
        when(file.isDelta()).thenReturn(true);
        caseLoaderService.process();
    }
}
