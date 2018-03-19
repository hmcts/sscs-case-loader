package uk.gov.hmcts.reform.sscs.services;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.doThrow;
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
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.refdata.RefDataFactory;
import uk.gov.hmcts.reform.sscs.services.ccd.CcdCasesSender;
import uk.gov.hmcts.reform.sscs.services.ccd.SearchCoreCaseDataService;
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
    private SearchCoreCaseDataService ccdCaseService;

    private CaseData caseData;

    private CaseLoaderService caseLoaderService;

    @Before
    public void setUp() {
        caseLoaderService = new CaseLoaderService(sftpSshService,
            xmlValidator,
            transformService,
            ccdCaseService,
            ccdCasesSender,
            refDataFactory);

        caseData = CaseData.builder()
            .caseReference("caseRef")
            .build();
    }

    @Test
    public void shouldLoadCasesGivenIncomingXmlFiles() {
        when(sftpSshService.getFiles()).thenReturn(newArrayList(file));
        when(sftpSshService.readExtractFile(file)).thenReturn(is);
        when(file.isDelta()).thenReturn(true);
        when(transformService.transform(is)).thenReturn(newArrayList(caseData));

        caseLoaderService.process();

        verify(xmlValidator).validateXml(file);
    }

    @Test
    public void shouldUpdateCaseGivenIncomingXmlFiles() {
        when(sftpSshService.getFiles()).thenReturn(newArrayList(file));
        when(sftpSshService.readExtractFile(file)).thenReturn(is);
        when(file.isDelta()).thenReturn(true);
        CaseDetails caseDetails = CaseDetails.builder().build();
        when(ccdCaseService.findCaseByCaseRef("caseRef")).thenReturn(newArrayList(caseDetails));
        when(transformService.transform(is)).thenReturn(newArrayList(caseData));

        caseLoaderService.process();

        verify(xmlValidator).validateXml(file);
    }

    @Test
    public void shouldProcessReferenceDataGivenFile() throws XMLStreamException {
        when(sftpSshService.getFiles()).thenReturn(newArrayList(file));
        when(sftpSshService.readExtractFile(file)).thenReturn(is);
        when(file.isDelta()).thenReturn(false);

        caseLoaderService.process();

        verify(xmlValidator).validateXml(file);
        verify(refDataFactory).extract(is);
    }

    @Test(expected = TransformException.class)
    public void shouldThrowExceptionGivenReferenceExtractFails() throws XMLStreamException {
        when(sftpSshService.getFiles()).thenReturn(newArrayList(file));
        when(sftpSshService.readExtractFile(file)).thenReturn(is);
        when(file.isDelta()).thenReturn(false);

        doThrow(new XMLStreamException()).when(refDataFactory).extract(is);

        caseLoaderService.process();

        verify(xmlValidator).validateXml(file);
    }
}
