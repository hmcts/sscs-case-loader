package uk.gov.hmcts.reform.sscs.services;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.sscs.models.GapsEvent.APPEAL_RECEIVED;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.models.GapsEvent;
import uk.gov.hmcts.reform.sscs.models.GapsInputStream;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Doc;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Documents;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Event;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Events;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Evidence;
import uk.gov.hmcts.reform.sscs.services.ccd.CcdCasesSender;
import uk.gov.hmcts.reform.sscs.services.ccd.CreateCoreCaseDataService;
import uk.gov.hmcts.reform.sscs.services.ccd.SearchCoreCaseDataService;
import uk.gov.hmcts.reform.sscs.services.ccd.UpdateCoreCaseDataService;
import uk.gov.hmcts.reform.sscs.services.mapper.TransformJsonCasesToCaseData;
import uk.gov.hmcts.reform.sscs.services.mapper.TransformXmlFilesToJsonFiles;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpSshService;
import uk.gov.hmcts.reform.sscs.services.xml.XmlValidator;

@RunWith(JUnitParamsRunner.class)
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

    @Test
    @Parameters({"APPEAL_RECEIVED", "RESPONSE_RECEIVED", "HEARING_BOOKED", "HEARING_POSTPONED", "APPEAL_LAPSED",
        "APPEAL_WITHDRAWN", "HEARING_ADJOURNED", "APPEAL_DORMANT"})
    public void givenFileWithAppealReceivedUpdate_shouldUpdateCcdCorrectly(GapsEvent gapsEvent) throws IOException {
        setupUpdateCaseMocks(gapsEvent);

        caseLoaderService.process();

        verify(updateCoreCaseDataService, times(1))
            .updateCase(any(CaseData.class), anyLong(), eq(gapsEvent.getType()));
    }

    @Test
    public void givenFileWithFurtherEvidence_shouldUpdateCcdTwice() throws IOException {
        when(sftpSshService.readExtractFiles()).thenReturn(buildGapsInputStreams());
        doNothing().when(xmlValidator).validateXml(anyString(), anyString());
        when(transformXmlFilesToJsonFiles.transform(anyString())).thenReturn(mock(JSONObject.class));

        CaseData caseData = buildUpdateCaseData(APPEAL_RECEIVED);

        List<CaseData> caseDataList = Collections.singletonList(caseData);
        when(transformJsonCasesToCaseData.transformUpdateCases(anyString())).thenReturn(caseDataList);

        CaseDetails existingCaseDetails = CaseDetails.builder().data(buildCcdDataMap()).build();
        List<CaseDetails> caseDetailsList = new ArrayList<>();
        caseDetailsList.add(existingCaseDetails);

        when(searchCoreCaseDataService.findCaseByCaseRef(anyString())).thenReturn(caseDetailsList);

        doReturn(existingCaseDetails)
            .when(updateCoreCaseDataService).updateCase(any(CaseData.class), anyLong(), eq(APPEAL_RECEIVED.getType()));

        caseLoaderService.process();

        verify(updateCoreCaseDataService, times(1))
            .updateCase(any(CaseData.class), anyLong(), eq("evidenceReceived"));

        verify(updateCoreCaseDataService, times(1))
            .updateCase(any(CaseData.class), anyLong(), eq("appealReceived"));
    }

    @Test
    public void givenFurtherEvidenceReceived_shouldUpdateCcdCorrectly() {

        CaseData newCaseData = CaseData.builder().evidence(buildEvidence()).build();

        CaseDetails existingCaseDetails = CaseDetails.builder().data(buildCcdDataMap()).build();

        caseLoaderService.checkNewEvidenceReceived(newCaseData, existingCaseDetails);

        verify(updateCoreCaseDataService, times(1))
            .updateCase(any(CaseData.class), anyLong(), eq("evidenceReceived"));
    }

    @Test
    public void givenNoNewFurtherEvidenceReceived_shouldNotUpdateCcd() {
        Map<String, Object> valueMap = new LinkedHashMap<>();
        Map<String, String> evidenceData = new LinkedHashMap<>();
        evidenceData.put("description", "1");
        evidenceData.put("dateReceived", "2017-05-24");
        valueMap.put("value", evidenceData);

        List<Map<String, Object>> documentsMap = new ArrayList<>();
        documentsMap.add(valueMap);

        Map<String, Object> evidenceMap = new LinkedHashMap<>();
        evidenceMap.put("documents", documentsMap);
        Map<String, Object> caseDataMap = new HashMap<>(1);
        caseDataMap.put("evidence", evidenceMap);

        CaseDetails existingCaseDetails = CaseDetails.builder().data(caseDataMap).build();

        Documents doc = Documents.builder()
            .value(Doc.builder()
                .description("1")
                .dateReceived("2017-05-24")
                .build())
            .build();

        Evidence evidence = Evidence.builder().documents(Collections.singletonList(doc)).build();
        CaseData newCaseData = CaseData.builder().evidence(evidence).build();

        caseLoaderService.checkNewEvidenceReceived(newCaseData, existingCaseDetails);

        verify(updateCoreCaseDataService, times(0))
            .updateCase(any(CaseData.class), anyLong(), eq("evidenceReceived"));
    }

    @Test
    public void givenLatestEventIsNull_shouldNotUpdateCcd() throws IOException {
        when(sftpSshService.readExtractFiles()).thenReturn(buildGapsInputStreams());
        doNothing().when(xmlValidator).validateXml(anyString(), anyString());
        when(transformXmlFilesToJsonFiles.transform(anyString())).thenReturn(mock(JSONObject.class));

        CaseData caseData = CaseData.builder().build();

        List<CaseData> caseDataList = Collections.singletonList(caseData);
        when(transformJsonCasesToCaseData.transformUpdateCases(anyString())).thenReturn(caseDataList);

        CaseDetails caseDetails = CaseDetails.builder().build();
        List<CaseDetails> caseDetailsList = new ArrayList<>();
        caseDetailsList.add(caseDetails);

        when(searchCoreCaseDataService.findCaseByCaseRef(anyString())).thenReturn(caseDetailsList);

        caseLoaderService.process();

        verify(updateCoreCaseDataService, times(0))
            .updateCase(any(CaseData.class), anyLong(), anyString());
    }

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

    private void setupUpdateCaseMocks(GapsEvent event) throws IOException {
        when(sftpSshService.readExtractFiles()).thenReturn(buildGapsInputStreams());
        doNothing().when(xmlValidator).validateXml(anyString(), anyString());
        when(transformXmlFilesToJsonFiles.transform(anyString())).thenReturn(mock(JSONObject.class));

        CaseData caseData = buildUpdateCaseData(event);

        List<CaseData> caseDataList = Collections.singletonList(caseData);
        when(transformJsonCasesToCaseData.transformUpdateCases(anyString())).thenReturn(caseDataList);

        Map<String, Object> caseDataMap = new HashMap<>(1);
        Map<String, Object> evidenceMap = new LinkedHashMap<>();
        evidenceMap.put("documents", new ArrayList<HashMap<String, Object>>());
        caseDataMap.put("evidence", evidenceMap);

        CaseDetails caseDetails = CaseDetails.builder().data(caseDataMap).build();
        List<CaseDetails> caseDetailsList = new ArrayList<>();
        caseDetailsList.add(caseDetails);

        when(searchCoreCaseDataService.findCaseByCaseRef(anyString())).thenReturn(caseDetailsList);

        doReturn(caseDetails)
            .when(updateCoreCaseDataService).updateCase(any(CaseData.class), anyLong(), eq(event.getType()));
    }

    private CaseData buildUpdateCaseData(GapsEvent event) {
        Event appealCreatedEvent = Event.builder()
            .type("appealCreated")
            .description("Appeal Created")
            .date("2018-01-14T21:59:43.10")
            .build();

        Event updateEvent = Event.builder()
            .type(event.getType())
            .description(event.getDescription())
            .date("2018-01-15T21:59:43.10")
            .build();

        List<Events> events = new ArrayList<>();

        events.add(Events.builder().value(appealCreatedEvent).build());
        events.add(Events.builder().value(updateEvent).build());

        Collections.sort(events, Collections.reverseOrder());

        return CaseData.builder().events(events).evidence(buildEvidence()).build();
    }

    private Evidence buildEvidence() {
        Doc document1 = Doc.builder()
            .description("1")
            .dateReceived("2017-05-24")
            .build();

        Doc document2 = Doc.builder()
            .description("Second evidence")
            .dateReceived("2017-05-25")
            .build();

        List<Documents> documents = new ArrayList<>();

        documents.add(Documents.builder().value(document1).build());
        documents.add(Documents.builder().value(document2).build());

        return Evidence.builder().documents(documents).build();
    }

    private Map<String, Object> buildCcdDataMap() {
        Map<String, Object> caseDataMap = new HashMap<>(1);
        Map<String, Object> evidenceMap = new LinkedHashMap<>();
        evidenceMap.put("documents", new ArrayList<HashMap<String, Object>>());
        caseDataMap.put("evidence", evidenceMap);

        return caseDataMap;
    }
}
