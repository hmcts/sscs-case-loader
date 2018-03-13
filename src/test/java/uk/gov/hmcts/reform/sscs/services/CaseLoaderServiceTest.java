package uk.gov.hmcts.reform.sscs.services;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.List;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.services.ccd.CcdCasesSender;
import uk.gov.hmcts.reform.sscs.services.gaps2.files.Gaps2File;
import uk.gov.hmcts.reform.sscs.services.mapper.TransformJsonCasesToCaseData;
import uk.gov.hmcts.reform.sscs.services.mapper.TransformXmlFilesToJsonFiles;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpSshService;
import uk.gov.hmcts.reform.sscs.services.xml.XmlValidator;

@RunWith(MockitoJUnitRunner.class)
public class CaseLoaderServiceTest {

    @Mock
    private Gaps2File file;
    @Mock
    private SftpSshService sftpSshService;
    @Mock
    private XmlValidator xmlValidator;
    @Mock
    private TransformXmlFilesToJsonFiles transformXmlFilesToJsonFiles;
    @Mock
    private TransformJsonCasesToCaseData transformJsonCasesToCaseData;
    @Mock
    private CcdCasesSender ccdCasesSender;

    @Mock
    private JSONObject jsonObj;

    private CaseLoaderService caseLoaderService;

    @Before
    public void setUp() {
        caseLoaderService = new CaseLoaderService(sftpSshService, xmlValidator, transformXmlFilesToJsonFiles,
            transformJsonCasesToCaseData,
            ccdCasesSender);
    }

    @Test
    public void shouldProcessBothCorrectlyGivenDeltaAndRefFiles() {
        when(sftpSshService.getFiles()).thenReturn(newArrayList(file, file));

        when(sftpSshService.readExtractFile(file))
            .thenReturn(new ByteArrayInputStream("abc".getBytes()))
            .thenReturn(new ByteArrayInputStream("123".getBytes()));

        when(file.isDelta()).thenReturn(true).thenReturn(true).thenReturn(false).thenReturn(false);

        when(transformXmlFilesToJsonFiles.transform("abc")).thenReturn(jsonObj);

        when(jsonObj.toString()).thenReturn("json");
        List<CaseData> caseDataList = Collections.singletonList(CaseData.builder().build());
        when(transformJsonCasesToCaseData.transformCreateCases("json")).thenReturn(caseDataList);
        when(transformJsonCasesToCaseData.transformUpdateCases("json")).thenReturn(caseDataList);

        caseLoaderService.process();

        verify(ccdCasesSender).sendCreateCcdCases(caseDataList);
        verify(ccdCasesSender).sendUpdateCcdCases(caseDataList);

        verify(xmlValidator).validateXml("abc", true);
        verify(xmlValidator).validateXml("123", false);
    }

}
