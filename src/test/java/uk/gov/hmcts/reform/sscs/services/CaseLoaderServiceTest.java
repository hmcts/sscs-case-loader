package uk.gov.hmcts.reform.sscs.services;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.sscs.models.GapsEvent.*;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.models.GapsEvent;
import uk.gov.hmcts.reform.sscs.models.GapsInputStream;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Event;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Events;
import uk.gov.hmcts.reform.sscs.services.ccd.CreateCoreCaseDataService;
import uk.gov.hmcts.reform.sscs.services.ccd.SearchCoreCaseDataService;
import uk.gov.hmcts.reform.sscs.services.ccd.UpdateCoreCaseDataService;
import uk.gov.hmcts.reform.sscs.services.mapper.TransformJsonCasesToCaseData;
import uk.gov.hmcts.reform.sscs.services.mapper.TransformXmlFilesToJsonFiles;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpSshService;
import uk.gov.hmcts.reform.sscs.services.xml.XmlValidator;

@RunWith(MockitoJUnitRunner.class)
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

    private CaseLoaderService caseLoaderService;

    @Before
    public void setUp() {
        caseLoaderService = new CaseLoaderService(sftpSshService, xmlValidator, transformXmlFilesToJsonFiles,
            transformJsonCasesToCaseData, createCoreCaseDataService,
            searchCoreCaseDataService, updateCoreCaseDataService);
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
    public void givenFileWithAppealReceivedUpdate_shouldUpdateCcdCorrectly() throws IOException {
        setupUpdateCaseMocks(APPEAL_RECEIVED);

        caseLoaderService.process();

        verify(updateCoreCaseDataService, times(1))
            .updateCase(any(CaseData.class), anyLong(), eq(APPEAL_RECEIVED.getType()));
    }

    @Test
    public void givenFileWithResponseReceivedUpdate_shouldUpdateCcdCorrectly() throws IOException {
        setupUpdateCaseMocks(RESPONSE_RECEIVED);

        caseLoaderService.process();

        verify(updateCoreCaseDataService, times(1))
            .updateCase(any(CaseData.class), anyLong(), eq(RESPONSE_RECEIVED.getType()));
    }

    @Test
    public void givenFileWithHearingBookedUpdate_shouldUpdateCcdCorrectly() throws IOException {
        setupUpdateCaseMocks(HEARING_BOOKED);

        caseLoaderService.process();

        verify(updateCoreCaseDataService, times(1))
            .updateCase(any(CaseData.class), anyLong(), eq(HEARING_BOOKED.getType()));
    }

    @Test
    public void givenFileWithHearingPostponedUpdate_shouldUpdateCcdCorrectly() throws IOException {
        setupUpdateCaseMocks(HEARING_POSTPONED);

        caseLoaderService.process();

        verify(updateCoreCaseDataService, times(1))
            .updateCase(any(CaseData.class), anyLong(), eq(HEARING_POSTPONED.getType()));
    }

    @Test
    public void givenFileWithAppealLapsedUpdate_shouldUpdateCcdCorrectly() throws IOException {
        setupUpdateCaseMocks(APPEAL_LAPSED);

        caseLoaderService.process();

        verify(updateCoreCaseDataService, times(1))
            .updateCase(any(CaseData.class), anyLong(), eq(APPEAL_LAPSED.getType()));
    }

    @Test
    public void givenFileWithAppealWithdrawnUpdate_shouldUpdateCcdCorrectly() throws IOException {
        setupUpdateCaseMocks(APPEAL_WITHDRAWN);

        caseLoaderService.process();

        verify(updateCoreCaseDataService, times(1))
            .updateCase(any(CaseData.class), anyLong(), eq(APPEAL_WITHDRAWN.getType()));
    }

    @Test
    public void givenFileWithHearingAdjournedUpdate_shouldUpdateCcdCorrectly() throws IOException {
        setupUpdateCaseMocks(HEARING_ADJOURNED);

        caseLoaderService.process();

        verify(updateCoreCaseDataService, times(1))
            .updateCase(any(CaseData.class), anyLong(), eq(HEARING_ADJOURNED.getType()));
    }

    @Test
    public void givenFileWithAppealDormantUpdate_shouldUpdateCcdCorrectly() throws IOException {
        setupUpdateCaseMocks(APPEAL_DORMANT);

        caseLoaderService.process();

        verify(updateCoreCaseDataService, times(1))
            .updateCase(any(CaseData.class), anyLong(), eq(APPEAL_DORMANT.getType()));
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

        CaseDetails caseDetails = CaseDetails.builder().build();
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
            .date("2018-01-14T21:59:43.10-05:00")
            .build();

        Event updateEvent = Event.builder()
            .type(event.getType())
            .description(event.getDescription())
            .date("2018-01-15T21:59:43.10-05:00")
            .build();

        List<Events> events = new ArrayList<>();

        events.add(Events.builder().value(appealCreatedEvent).build());
        events.add(Events.builder().value(updateEvent).build());

        Collections.sort(events, Collections.reverseOrder());

        return CaseData.builder().events(events).build();

    }
}
