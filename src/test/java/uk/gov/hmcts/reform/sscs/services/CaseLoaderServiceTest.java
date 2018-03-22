package uk.gov.hmcts.reform.sscs.services;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import javax.xml.stream.XMLStreamException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.exceptions.TransformException;
import uk.gov.hmcts.reform.sscs.models.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Appeal;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.BenefitType;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.refdata.RefDataFactory;
import uk.gov.hmcts.reform.sscs.services.ccd.CcdCasesSender;
import uk.gov.hmcts.reform.sscs.services.ccd.SearchCcdService;
import uk.gov.hmcts.reform.sscs.services.gaps2.files.Gaps2File;
import uk.gov.hmcts.reform.sscs.services.idam.IdamService;
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

    private CaseData caseData;

    private CaseLoaderService caseLoaderService;

    @Before
    public void setUp() {
        when(sftpSshService.getFiles()).thenReturn(newArrayList(file, file));
        stub(sftpSshService.readExtractFile(file)).toReturn(is);
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
        caseData = CaseData.builder()
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
    public void shouldUpdateCaseGivenIncomingXmlFiles() {
        CaseDetails caseDetails = CaseDetails.builder().build();
        IdamTokens idamTokens = IdamTokens.builder()
            .idamOauth2Token("idamOauth2Token")
            .authenticationService("serviceAuthorization")
            .build();
        when(ccdCaseService.findCaseByCaseRef(eq("caseRef"), eq(idamTokens)))
            .thenReturn(newArrayList(caseDetails));
        when(transformService.transform(is)).thenReturn(newArrayList(caseData));
        when(idamService.getIdamOauth2Token()).thenReturn("idamOauth2Token");
        when(idamService.generateServiceAuthorization()).thenReturn("serviceAuthorization");

        caseLoaderService.process();

        verify(xmlValidator, times(2)).validateXml(file);
        verify(ccdCaseService).findCaseByCaseRef(eq("caseRef"), eq(idamTokens));
        verify(sftpSshService, times(2)).move(file, true);
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
