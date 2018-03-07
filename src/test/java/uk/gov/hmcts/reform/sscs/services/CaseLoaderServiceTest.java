package uk.gov.hmcts.reform.sscs.services;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.models.GapsInputStream;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.services.ccd.CcdCasesSender;
import uk.gov.hmcts.reform.sscs.services.ccd.CreateCoreCaseDataService;
import uk.gov.hmcts.reform.sscs.services.ccd.SearchCoreCaseDataService;
import uk.gov.hmcts.reform.sscs.services.ccd.UpdateCoreCaseDataService;
import uk.gov.hmcts.reform.sscs.services.mapper.TransformJsonCasesToCaseData;
import uk.gov.hmcts.reform.sscs.services.mapper.TransformXmlFilesToJsonFiles;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpSshService;
import uk.gov.hmcts.reform.sscs.services.xml.XmlValidator;

public class CaseLoaderServiceTest {

    @Mock
    private SftpSshService sftpSshService;
    @Mock
    private XmlValidator xmlValidator;
    @Mock
    private TransformXmlFilesToJsonFiles transformXmlFilesToJsonFiles;
    @Mock
    private TransformJsonCasesToCaseData transformJsonCasesToCaseData;
    @Mock
    private CreateCoreCaseDataService createCoreCaseDataService;
    @Mock
    private SearchCoreCaseDataService searchCoreCaseDataService;
    @Mock
    private UpdateCoreCaseDataService updateCoreCaseDataService;
    @Mock
    private CcdCasesSender ccdCasesSender;

    private CaseLoaderService caseLoaderService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        caseLoaderService = new CaseLoaderService(sftpSshService, xmlValidator, transformXmlFilesToJsonFiles,
            transformJsonCasesToCaseData,
            ccdCasesSender);
    }

    @Test
    public void givenDeltaAndRefFiles_shouldProcessBothCorrectly() throws IOException {
        when(sftpSshService.readExtractFiles()).thenReturn(buildGapsInputStreams());
        doNothing().when(xmlValidator).validateXml(anyString(), anyString());
        when(transformXmlFilesToJsonFiles.transform(anyString())).thenReturn(mock(JSONObject.class));
        List<CaseData> caseDataList = Collections.singletonList(CaseData.builder().build());
        when(transformJsonCasesToCaseData.transformCreateCases(anyString())).thenReturn(caseDataList);
        when(createCoreCaseDataService.createCcdCase(any(CaseData.class)))
            .thenReturn(CaseDetails.builder().build());
        caseLoaderService.process();
    }

    //    @Test
    //    public void givenFileWithFurtherEvidence_shouldUpdateCcdTwice() throws IOException {
    //        when(sftpSshService.readExtractFiles()).thenReturn(buildGapsInputStreams());
    //        doNothing().when(xmlValidator).validateXml(anyString(), anyString());
    //        when(transformXmlFilesToJsonFiles.transform(anyString())).thenReturn(mock(JSONObject.class));
    //
    //        CaseData caseData = buildUpdateCaseData(APPEAL_RECEIVED);
    //
    //        List<CaseData> caseDataList = Collections.singletonList(caseData);
    //        when(transformJsonCasesToCaseData.transformUpdateCases(anyString())).thenReturn(caseDataList);
    //
    //        CaseDetails existingCaseDetails = CaseDetails.builder().data(buildCcdDataMap()).build();
    //        List<CaseDetails> caseDetailsList = new ArrayList<>();
    //        caseDetailsList.add(existingCaseDetails);
    //
    //        when(searchCoreCaseDataService.findCaseByCaseRef(anyString())).thenReturn(caseDetailsList);
    //
    //        doReturn(existingCaseDetails)
    //            .when(updateCoreCaseDataService).updateCase(any(CaseData.class), anyLong(),
    // eq(APPEAL_RECEIVED.getType()));
    //
    //        caseLoaderService.process();
    //
    //        verify(updateCoreCaseDataService, times(1))
    //            .updateCase(any(CaseData.class), anyLong(), eq("evidenceReceived"));
    //
    //        verify(updateCoreCaseDataService, times(1))
    //            .updateCase(any(CaseData.class), anyLong(), eq("appealReceived"));
    //    }
    //
    //    @Test
    //    public void givenFurtherEvidenceReceived_shouldUpdateCcdCorrectly() {
    //
    //        CaseData newCaseData = CaseData.builder().evidence(buildEvidence()).build();
    //
    //        CaseDetails existingCaseDetails = CaseDetails.builder().data(buildCcdDataMap()).build();
    //
    //        caseLoaderService.checkNewEvidenceReceived(newCaseData, existingCaseDetails);
    //
    //        verify(updateCoreCaseDataService, times(1))
    //            .updateCase(any(CaseData.class), anyLong(), eq("evidenceReceived"));
    //    }

    //    @Test
    //    public void givenNoNewFurtherEvidenceReceived_shouldNotUpdateCcd() {
    //        Map<String, Object> valueMap = new LinkedHashMap<>();
    //        Map<String, String> evidenceData = new LinkedHashMap<>();
    //        evidenceData.put("description", "1");
    //        evidenceData.put("dateReceived", "2017-05-24");
    //        valueMap.put("value", evidenceData);
    //
    //        List<Map<String, Object>> documentsMap = new ArrayList<>();
    //        documentsMap.add(valueMap);
    //
    //        Map<String, Object> evidenceMap = new LinkedHashMap<>();
    //        evidenceMap.put("documents", documentsMap);
    //        Map<String, Object> caseDataMap = new HashMap<>(1);
    //        caseDataMap.put("evidence", evidenceMap);
    //
    //        CaseDetails existingCaseDetails = CaseDetails.builder().data(caseDataMap).build();
    //
    //        Documents doc = Documents.builder()
    //            .value(Doc.builder()
    //                .description("1")
    //                .dateReceived("2017-05-24")
    //                .build())
    //            .build();
    //
    //        Evidence evidence = Evidence.builder().documents(Collections.singletonList(doc)).build();
    //        CaseData newCaseData = CaseData.builder().evidence(evidence).build();
    //
    //        caseLoaderService.checkNewEvidenceReceived(newCaseData, existingCaseDetails);
    //
    //        verify(updateCoreCaseDataService, times(0))
    //            .updateCase(any(CaseData.class), anyLong(), eq("evidenceReceived"));
    //    }



    private List<GapsInputStream> buildGapsInputStreams() throws IOException {
        GapsInputStream refStream = GapsInputStream.builder()
            .isDelta(false)
            .isReference(true)
            .inputStream(IOUtils.toInputStream("Reference", StandardCharsets.UTF_8.name()))
            .build();
        GapsInputStream deltaStream = GapsInputStream.builder()
            .isDelta(true)
            .isReference(false)
            .inputStream(IOUtils.toInputStream("Delta", StandardCharsets.UTF_8.name()))
            .build();
        return ImmutableList.of(refStream, deltaStream);
    }


}
