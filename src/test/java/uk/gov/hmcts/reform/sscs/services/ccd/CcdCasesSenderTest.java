package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.reform.sscs.CaseDetailsUtils.getSscsCaseDetails;
import static uk.gov.hmcts.reform.sscs.ccd.domain.EventType.*;
import static uk.gov.hmcts.reform.sscs.models.GapsEvent.APPEAL_RECEIVED;
import static uk.gov.hmcts.reform.sscs.models.GapsEvent.RESPONSE_RECEIVED;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import junitparams.JUnitParamsRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.sscs.ccd.client.CcdClient;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appeal;
import uk.gov.hmcts.reform.sscs.ccd.domain.Appellant;
import uk.gov.hmcts.reform.sscs.ccd.domain.BenefitType;
import uk.gov.hmcts.reform.sscs.ccd.domain.Contact;
import uk.gov.hmcts.reform.sscs.ccd.domain.Document;
import uk.gov.hmcts.reform.sscs.ccd.domain.DocumentDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.Event;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.EventType;
import uk.gov.hmcts.reform.sscs.ccd.domain.Evidence;
import uk.gov.hmcts.reform.sscs.ccd.domain.Hearing;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.HearingOptions;
import uk.gov.hmcts.reform.sscs.ccd.domain.Identity;
import uk.gov.hmcts.reform.sscs.ccd.domain.Name;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.SscsCcdConvertService;
import uk.gov.hmcts.reform.sscs.ccd.service.UpdateCcdCaseService;
import uk.gov.hmcts.reform.sscs.ccd.service.UpdateCcdCaseService.ConditionalUpdateResult;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.job.DataMigrationJob;
import uk.gov.hmcts.reform.sscs.models.GapsEvent;
import uk.gov.hmcts.reform.sscs.models.UpdateType;

@RunWith(JUnitParamsRunner.class)
public class CcdCasesSenderTest {

    static final String CASE_DETAILS_JSON = "CaseDetailsWithOneEventAndNoEvidence.json";
    private static final String CASE_DETAILS_WITH_ONE_EVIDENCE_AND_ONE_EVENT_JSON =
        "CaseDetailsWithOneEvidenceAndOneEvent.json";
    private static final String OAUTH2 = "token";
    private static final String SERVICE_AUTHORIZATION = "serviceAuthorization";
    private static final String USER_ID = "16";
    private static final String CASE_DETAILS_WITH_HEARINGS_JSON = "CaseDetailsWithHearings.json";
    private static final String CASE_DETAILS_WITH_NO_HEARINGS_JSON = "CaseDetailsWithNoHearings.json";
    private static final String CASE_DETAILS_WITH_HEARING_OPTIONS_JSON = "CaseDetailsWithHearingOptions.json";
    public static final String CASE_DETAILS_WITH_APPEAL_RECEIVED_JSON = "CaseDetailsWithAppealReceived.json";
    private static final String SSCS_APPEAL_UPDATED_EVENT = "SSCS - appeal updated event";
    private static final String UPDATED_SSCS = "Updated SSCS";

    @Mock
    private UpdateCcdCaseService updateCcdCaseService;
    @Mock
    private UpdateCcdCaseData updateCcdCaseData;
    @Mock
    private CcdClient ccdClient;
    @Mock
    private SscsCcdConvertService sscsCcdConvertService;

    @Mock
    private DataMigrationJob migrationJob;

    private CcdCasesSender ccdCasesSender;
    private IdamTokens idamTokens;

    @Captor
    private ArgumentCaptor<Consumer<SscsCaseDetails>> caseDetailsCaptor;

    @Captor
    private ArgumentCaptor<Function<SscsCaseDetails, ConditionalUpdateResult>> conditionalCaseDetailsCaptor;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ccdCasesSender = new CcdCasesSender(updateCcdCaseService, updateCcdCaseData, ccdClient, sscsCcdConvertService);
        idamTokens = IdamTokens.builder()
            .idamOauth2Token(OAUTH2)
            .serviceAuthorization(SERVICE_AUTHORIZATION)
            .userId(USER_ID)
            .build();

        given(updateCcdCaseData.updateCcdRecordForChangesAndReturnUpdateType(any(), any()))
            .willReturn(UpdateType.EVENT_UPDATE);
    }

    @Test
    public void givenACaseUpdate_shouldOnlyOverwriteFieldsThatExistInGapsData() throws Exception {
        SscsCaseData caseData = buildTestCaseDataWithAppellantAndBenefitType();
        Appellant appellant = caseData.getAppeal().getAppellant();
        BenefitType benefitType = caseData.getAppeal().getBenefitType();

        SscsCaseDetails existingCaseDetails = getSscsCaseDetails(CASE_DETAILS_WITH_HEARING_OPTIONS_JSON);

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails, idamTokens);

        assertEquals(appellant, caseData.getAppeal().getAppellant());
        assertEquals(benefitType, caseData.getAppeal().getBenefitType());

        assertNull(caseData.getAppeal().getMrnDetails());
        assertNull(caseData.getAppeal().getAppealReasons());
        assertNull(caseData.getAppeal().getHearingOptions());
        assertNull(caseData.getAppeal().getRep());
        assertNull(caseData.getAppeal().getSigner());
    }

    @Test
    void givenACaseUpdate_shouldOnlyOverwriteFieldsThatExistInGapsDataV2() throws Exception {
        setField(ccdCasesSender, "updateCaseV2Enabled", true);
        SscsCaseData caseData = buildTestCaseDataWithAppellantAndBenefitType();

        SscsCaseDetails existingCaseDetails = getSscsCaseDetails(CASE_DETAILS_WITH_HEARING_OPTIONS_JSON);

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails, idamTokens);

        verify(updateCcdCaseService).updateCaseV2(
            eq(existingCaseDetails.getId()), eq(EventType.APPEAL_RECEIVED.getCcdType()),
            eq(SSCS_APPEAL_UPDATED_EVENT),
            eq(UPDATED_SSCS),
            eq(idamTokens),
            caseDetailsCaptor.capture());

        caseDetailsCaptor.getValue().accept(existingCaseDetails);

        verify(updateCcdCaseData, times(2))
            .updateCcdRecordForChangesAndReturnUpdateType(
                caseData,
                existingCaseDetails.getData());

        Appellant appellant = caseData.getAppeal().getAppellant();
        BenefitType benefitType = caseData.getAppeal().getBenefitType();
        assertEquals(appellant, caseData.getAppeal().getAppellant());
        assertEquals(benefitType, caseData.getAppeal().getBenefitType());
        assertNull(caseData.getAppeal().getMrnDetails());
        assertNull(caseData.getAppeal().getAppealReasons());
        assertNull(caseData.getAppeal().getHearingOptions());
        assertNull(caseData.getAppeal().getRep());
        assertNull(caseData.getAppeal().getSigner());

        verify(updateCcdCaseService, never()).updateCase(any(), anyLong(), any(), any(), any(), any());

    }

    @Test
    public void shouldUpdateLanguage() {
        var caseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingOptions(HearingOptions.builder().languages("Swahili").build()).build()
            ).build();
        var caseDetails = CaseDetails.builder().state("ReadyToList").data(new HashMap<>()).build();
        var startEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails)
            .eventId("migrateCase")
            .token("random-token").build();
        when(ccdClient.startEvent(eq(idamTokens), anyLong(), eq("migrateCase"))).thenReturn(startEventResponse);
        when(sscsCcdConvertService.getCaseData(anyMap())).thenReturn(caseData);

        ccdCasesSender.updateCaseMigration(1L, idamTokens, "Somali", migrationJob);

        verify(updateCcdCaseService).updateCase(
            eq(caseData), anyLong(), eq("migrateCase"), eq("random-token"),
            eq("migrateCase"), eq(""), eq(""), eq(idamTokens)
        );
    }

    @Test
    void shouldUpdateLanguageV2() {
        setField(ccdCasesSender, "updateCaseV2Enabled", true);
        var caseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingOptions(HearingOptions.builder().languages("Swahili").build()).build()
            ).build();

        ccdCasesSender.updateCaseMigration(1L, idamTokens, "Somali", migrationJob);

        verify(updateCcdCaseService).updateCaseV2Conditional(
            anyLong(),
            eq("migrateCase"),
            eq(idamTokens), conditionalCaseDetailsCaptor.capture()
        );

        SscsCaseDetails sscsCaseDetails = SscsCaseDetails.builder().data(caseData).build();
        ConditionalUpdateResult conditionalUpdateResult = conditionalCaseDetailsCaptor.getValue().apply(sscsCaseDetails);
        assertThat(conditionalUpdateResult.willCommit(), equalTo(true));

        verify(updateCcdCaseService, never()).updateCase(
            eq(caseData), anyLong(), eq("migrateCase"), eq("random-token"),
            eq("migrateCase"), eq(""), eq(""), eq(idamTokens)
        );
    }

    @Test
    void shouldNotUpdateLanguageV2() {
        setField(ccdCasesSender, "updateCaseV2Enabled", true);
        var caseData = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .hearingOptions(HearingOptions.builder().languages("Swahili").build()).build()
            ).build();

        when(migrationJob.shouldBeSkipped(any(), any())).thenReturn(true);
        ccdCasesSender.updateCaseMigration(1L, idamTokens, "Somali", migrationJob);

        verify(updateCcdCaseService).updateCaseV2Conditional(
            anyLong(),
            eq("migrateCase"),
            eq(idamTokens), conditionalCaseDetailsCaptor.capture()
        );

        SscsCaseDetails sscsCaseDetails = SscsCaseDetails.builder().data(caseData).build();
        ConditionalUpdateResult conditionalUpdateResult = conditionalCaseDetailsCaptor.getValue().apply(sscsCaseDetails);
        assertThat(conditionalUpdateResult.willCommit(), equalTo(false));

        verify(updateCcdCaseService, never()).updateCase(
            eq(caseData), anyLong(), eq("migrateCase"), eq("random-token"),
            eq("migrateCase"), eq(""), eq(""), eq(idamTokens)
        );
    }

    @ParameterizedTest()
    @MethodSource("generateGapsCaseDataScenarios")
    void givenACaseUpdate_shouldNotThrowNullPointExceptionIfAppealOrAppellantIsNull(SscsCaseData gapsCaseData)
        throws Exception {
        SscsCaseDetails existingCaseDetails = getSscsCaseDetails(CASE_DETAILS_WITH_HEARING_OPTIONS_JSON);
        ccdCasesSender.sendUpdateCcdCases(gapsCaseData, existingCaseDetails, idamTokens);
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private static Stream<SscsCaseData> generateGapsCaseDataScenarios() {
        SscsCaseData gapsCaseDataWithNullAppeal = SscsCaseData.builder()
            .appeal(null)
            .build();

        SscsCaseData gapsCaseDataWithNullAppellant = SscsCaseData.builder()
            .appeal(Appeal.builder()
                .appellant(null)
                .build())
            .build();

        return Stream.of(gapsCaseDataWithNullAppeal, gapsCaseDataWithNullAppellant);
    }

    @Test
    public void shouldOverrideEventToAppealReceivedGivenThereIsACaseReferenceHasBeenAdded() throws Exception {
        SscsCaseDetails sscsCaseDetails = getSscsCaseDetails(CASE_DETAILS_JSON);
        sscsCaseDetails.getData().setCaseReference(null);
        given(updateCcdCaseData.updateCcdRecordForChangesAndReturnUpdateType(any(), any()))
            .willReturn(UpdateType.DATA_UPDATE);

        ccdCasesSender.sendUpdateCcdCases(buildCaseData(RESPONSE_RECEIVED),
            sscsCaseDetails, idamTokens);

        verify(updateCcdCaseService, times(1))
            .updateCase(eq(sscsCaseDetails.getData()), anyLong(), eq(APPEAL_RECEIVED.getType()),
                eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));
    }

    @Test
    void shouldOverrideEventToAppealReceivedGivenThereIsACaseReferenceHasBeenAddedV2() throws Exception {
        setField(ccdCasesSender, "updateCaseV2Enabled", true);

        SscsCaseDetails sscsCaseDetails = getSscsCaseDetails(CASE_DETAILS_JSON);
        sscsCaseDetails.getData().setCaseReference(null);
        given(updateCcdCaseData.updateCcdRecordForChangesAndReturnUpdateType(any(), any()))
            .willReturn(UpdateType.DATA_UPDATE);
        SscsCaseData sscsCaseData = buildCaseData(RESPONSE_RECEIVED);
        ccdCasesSender.sendUpdateCcdCases(sscsCaseData,
            sscsCaseDetails, idamTokens);

        verify(updateCcdCaseService).updateCaseV2(
            eq(sscsCaseDetails.getId()),
            eq(EventType.APPEAL_RECEIVED.getCcdType()),
            eq(SSCS_APPEAL_UPDATED_EVENT),
            eq(UPDATED_SSCS),
            eq(idamTokens),
            caseDetailsCaptor.capture());

        caseDetailsCaptor.getValue().accept(sscsCaseDetails);
        assertThat(sscsCaseDetails.getData().getCaseReference(), equalTo(sscsCaseData.getCaseReference()));

        verify(updateCcdCaseData, times(2))
            .updateCcdRecordForChangesAndReturnUpdateType(
                sscsCaseData,
                sscsCaseDetails.getData());

        verify(updateCcdCaseService, never())
            .updateCase(eq(sscsCaseDetails.getData()), anyLong(), eq(APPEAL_RECEIVED.getType()),
                eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));
    }

    @Test
    public void shouldNotOverrideEventToAppealReceivedGivenThereIsACaseReferenceHasBeenAddedAndNewEvent()
        throws Exception {
        SscsCaseDetails sscsCaseDetails = getSscsCaseDetails(CASE_DETAILS_JSON);
        sscsCaseDetails.getData().setCaseReference(null);
        given(updateCcdCaseData.updateCcdRecordForChangesAndReturnUpdateType(any(), any()))
            .willReturn(UpdateType.EVENT_UPDATE);

        ccdCasesSender.sendUpdateCcdCases(buildCaseData(RESPONSE_RECEIVED),
            sscsCaseDetails, idamTokens);

        verify(updateCcdCaseService, times(1))
            .updateCase(eq(sscsCaseDetails.getData()), anyLong(), eq(RESPONSE_RECEIVED.getType()),
                eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));
    }

    @Test
    void shouldNotOverrideEventToAppealReceivedGivenThereIsACaseReferenceHasBeenAddedAndNewEventV2()
        throws Exception {
        setField(ccdCasesSender, "updateCaseV2Enabled", true);

        SscsCaseDetails sscsCaseDetails = getSscsCaseDetails(CASE_DETAILS_JSON);
        sscsCaseDetails.getData().setCaseReference(null);
        given(updateCcdCaseData.updateCcdRecordForChangesAndReturnUpdateType(any(), any()))
            .willReturn(UpdateType.EVENT_UPDATE);
        SscsCaseData sscsCaseData = buildCaseData(RESPONSE_RECEIVED);

        ccdCasesSender.sendUpdateCcdCases(sscsCaseData,
            sscsCaseDetails, idamTokens);

        verify(updateCcdCaseService).updateCaseV2(
            eq(sscsCaseDetails.getId()),
            eq(DWP_RESPOND.getCcdType()),
            eq(SSCS_APPEAL_UPDATED_EVENT),
            eq(UPDATED_SSCS),
            eq(idamTokens),
            caseDetailsCaptor.capture());

        caseDetailsCaptor.getValue().accept(sscsCaseDetails);
        assertThat(sscsCaseDetails.getData().getCaseReference(), equalTo(sscsCaseData.getCaseReference()));

        verify(updateCcdCaseData, times(2))
            .updateCcdRecordForChangesAndReturnUpdateType(
                sscsCaseData,
                sscsCaseDetails.getData());

        verify(updateCcdCaseService, never())
            .updateCase(eq(sscsCaseDetails.getData()), anyLong(), eq(RESPONSE_RECEIVED.getType()),
                eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));
    }

    @ParameterizedTest
    @ValueSource(strings = {"APPEAL_RECEIVED", "RESPONSE_RECEIVED", "HEARING_BOOKED", "HEARING_POSTPONED",
        "APPEAL_LAPSED", "APPEAL_WITHDRAWN", "HEARING_ADJOURNED", "APPEAL_DORMANT"})
    public void shouldUpdateCcdGivenThereIsAnEventChange(GapsEvent gapsEvent) throws Exception {
        SscsCaseDetails sscsCaseDetails = getSscsCaseDetails(CASE_DETAILS_JSON);
        ccdCasesSender.sendUpdateCcdCases(buildCaseData(gapsEvent),
            sscsCaseDetails, idamTokens);

        verify(updateCcdCaseService, times(1))
            .updateCase(eq(sscsCaseDetails.getData()), anyLong(), eq(gapsEvent.getType()),
                eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));
    }

    @ParameterizedTest
    @ValueSource(strings = {"APPEAL_RECEIVED", "RESPONSE_RECEIVED", "HEARING_BOOKED", "HEARING_POSTPONED",
        "APPEAL_LAPSED", "APPEAL_WITHDRAWN", "HEARING_ADJOURNED", "APPEAL_DORMANT"})
    void shouldUpdateCcdGivenThereIsAnEventChangeV2(GapsEvent gapsEvent) throws Exception {
        setField(ccdCasesSender, "updateCaseV2Enabled", true);

        SscsCaseDetails sscsCaseDetails = getSscsCaseDetails(CASE_DETAILS_JSON);
        SscsCaseData sscsCaseData = buildCaseData(gapsEvent);
        ccdCasesSender.sendUpdateCcdCases(sscsCaseData,
            sscsCaseDetails, idamTokens);

        verify(updateCcdCaseService).updateCaseV2(
            eq(sscsCaseDetails.getId()),
            eq(gapsEvent.getType()),
            eq(SSCS_APPEAL_UPDATED_EVENT),
            eq(UPDATED_SSCS),
            eq(idamTokens),
            caseDetailsCaptor.capture());

        caseDetailsCaptor.getValue().accept(sscsCaseDetails);

        verify(updateCcdCaseData, times(2))
            .updateCcdRecordForChangesAndReturnUpdateType(
                sscsCaseData,
                sscsCaseDetails.getData());

        verify(updateCcdCaseService, never())
            .updateCase(eq(sscsCaseDetails.getData()), anyLong(), eq(gapsEvent.getType()),
                eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));
    }

    @ParameterizedTest
    @ValueSource(strings = {"RESPONSE_RECEIVED", "APPEAL_RECEIVED"})
    public void givenDigitalCaseAndPreGapsEvent_thenTriggerCaseUpdatedEvent(GapsEvent gapsEvent)
        throws IOException {
        SscsCaseDetails sscsCaseDetails = getSscsCaseDetails(CASE_DETAILS_JSON);
        sscsCaseDetails.getData().setCreatedInGapsFrom(READY_TO_LIST.getCcdType());
        SscsCaseData caseData = buildCaseData(gapsEvent);
        ccdCasesSender.sendUpdateCcdCases(caseData, sscsCaseDetails, idamTokens);

        verify(updateCcdCaseService, times(1))
            .updateCase(eq(sscsCaseDetails.getData()), anyLong(), eq(CASE_UPDATED.getCcdType()),
                eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));
    }

    @ParameterizedTest
    @ValueSource(strings = {"RESPONSE_RECEIVED", "APPEAL_RECEIVED"})
    void givenDigitalCaseAndPreGapsEvent_thenTriggerCaseUpdatedEventV2(GapsEvent gapsEvent)
        throws IOException {
        setField(ccdCasesSender, "updateCaseV2Enabled", true);

        SscsCaseDetails sscsCaseDetails = getSscsCaseDetails(CASE_DETAILS_JSON);
        sscsCaseDetails.getData().setCreatedInGapsFrom(READY_TO_LIST.getCcdType());
        SscsCaseData caseData = buildCaseData(gapsEvent);
        ccdCasesSender.sendUpdateCcdCases(caseData, sscsCaseDetails, idamTokens);

        verify(updateCcdCaseService).updateCaseV2(
            eq(sscsCaseDetails.getId()),
            eq(CASE_UPDATED.getCcdType()),
            eq(SSCS_APPEAL_UPDATED_EVENT),
            eq(UPDATED_SSCS),
            eq(idamTokens),
            caseDetailsCaptor.capture());

        caseDetailsCaptor.getValue().accept(sscsCaseDetails);

        verify(updateCcdCaseData, times(2))
            .updateCcdRecordForChangesAndReturnUpdateType(
                caseData,
                sscsCaseDetails.getData());

        verify(updateCcdCaseService, never())
            .updateCase(eq(sscsCaseDetails.getData()), anyLong(), eq(gapsEvent.getType()),
                eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));
    }

    @Test
    public void givenNonDigitalCaseAndGapsEvent_thenProcessGapsEventr_response_received_dwp_respond()
        throws IOException {
        GapsEvent gapsEvent = RESPONSE_RECEIVED;
        EventType eventType = EventType.DWP_RESPOND;
        SscsCaseDetails sscsCaseDetails = getSscsCaseDetails(CASE_DETAILS_JSON);
        SscsCaseData caseData = buildCaseData(gapsEvent);
        caseData.setCreatedInGapsFrom(VALID_APPEAL.getCcdType());
        ccdCasesSender.sendUpdateCcdCases(caseData, sscsCaseDetails, idamTokens);

        verify(updateCcdCaseService, times(1))
            .updateCase(eq(sscsCaseDetails.getData()), anyLong(), eq(eventType.getCcdType()),
                eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));
    }

    @Test
    void givenNonDigitalCaseAndGapsEvent_thenProcessGapsEventr_response_received_dwp_respondV2()
        throws IOException {
        setField(ccdCasesSender, "updateCaseV2Enabled", true);

        GapsEvent gapsEvent = RESPONSE_RECEIVED;
        EventType eventType = EventType.DWP_RESPOND;
        SscsCaseDetails sscsCaseDetails = getSscsCaseDetails(CASE_DETAILS_JSON);
        SscsCaseData caseData = buildCaseData(gapsEvent);
        caseData.setCreatedInGapsFrom(VALID_APPEAL.getCcdType());
        ccdCasesSender.sendUpdateCcdCases(caseData, sscsCaseDetails, idamTokens);

        verify(updateCcdCaseService).updateCaseV2(
            eq(sscsCaseDetails.getId()),
            eq(eventType.getCcdType()),
            eq(SSCS_APPEAL_UPDATED_EVENT),
            eq(UPDATED_SSCS),
            eq(idamTokens),
            caseDetailsCaptor.capture());

        caseDetailsCaptor.getValue().accept(sscsCaseDetails);

        verify(updateCcdCaseData, times(2))
            .updateCcdRecordForChangesAndReturnUpdateType(
                caseData,
                sscsCaseDetails.getData());

        verify(updateCcdCaseService, never())
            .updateCase(eq(sscsCaseDetails.getData()), anyLong(), eq(eventType.getCcdType()),
                eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));
    }

    @Test
    public void givenNonDigitalCaseAndGapsEvent_thenProcessGapsEvent_appeal_received_appeal_received()
        throws IOException {
        GapsEvent gapsEvent = APPEAL_RECEIVED;
        EventType eventType = EventType.APPEAL_RECEIVED;
        SscsCaseDetails sscsCaseDetails = getSscsCaseDetails(CASE_DETAILS_JSON);
        SscsCaseData caseData = buildCaseData(gapsEvent);
        caseData.setCreatedInGapsFrom(VALID_APPEAL.getCcdType());
        ccdCasesSender.sendUpdateCcdCases(caseData, sscsCaseDetails, idamTokens);

        verify(updateCcdCaseService, times(1))
            .updateCase(eq(sscsCaseDetails.getData()), anyLong(), eq(eventType.getCcdType()),
                eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));
    }

    @Test
    void givenNonDigitalCaseAndGapsEvent_thenProcessGapsEvent_appeal_received_appeal_receivedV2()
        throws IOException {
        setField(ccdCasesSender, "updateCaseV2Enabled", true);

        GapsEvent gapsEvent = APPEAL_RECEIVED;
        EventType eventType = EventType.APPEAL_RECEIVED;
        SscsCaseDetails sscsCaseDetails = getSscsCaseDetails(CASE_DETAILS_JSON);
        SscsCaseData caseData = buildCaseData(gapsEvent);
        caseData.setCreatedInGapsFrom(VALID_APPEAL.getCcdType());
        ccdCasesSender.sendUpdateCcdCases(caseData, sscsCaseDetails, idamTokens);

        verify(updateCcdCaseService).updateCaseV2(
            eq(sscsCaseDetails.getId()),
            eq(eventType.getCcdType()),
            eq(SSCS_APPEAL_UPDATED_EVENT),
            eq(UPDATED_SSCS),
            eq(idamTokens),
            caseDetailsCaptor.capture());

        caseDetailsCaptor.getValue().accept(sscsCaseDetails);

        verify(updateCcdCaseData, times(2))
            .updateCcdRecordForChangesAndReturnUpdateType(
                caseData,
                sscsCaseDetails.getData());

        verify(updateCcdCaseService, never())
            .updateCase(eq(sscsCaseDetails.getData()), anyLong(), eq(eventType.getCcdType()),
                eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));
    }

    @Test
    public void shouldUpdateCcdGivenThereIsADataChange() {
        SscsCaseData caseData = SscsCaseData.builder()
            .events(Collections.singletonList(Event.builder()
                .value(EventDetails.builder()
                    .type(APPEAL_RECEIVED.getType())
                    .date("2017-05-23T13:18:15.073")
                    .description("Appeal received")
                    .build())
                .build()))
            .build();

        given(updateCcdCaseData.updateCcdRecordForChangesAndReturnUpdateType(any(), any()))
            .willReturn(UpdateType.DATA_UPDATE);

        SscsCaseDetails existingCcdCase = SscsCaseDetails.builder()
            .id(1L)
            .data(SscsCaseData.builder().build())
            .build();

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCcdCase, idamTokens);

        verify(updateCcdCaseService, times(1))
            .updateCase(any(SscsCaseData.class), anyLong(), eq("caseUpdated"),
                eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));
    }

    @Test
    void shouldUpdateCcdGivenThereIsADataChangeV2() {
        setField(ccdCasesSender, "updateCaseV2Enabled", true);

        SscsCaseData caseData = SscsCaseData.builder()
            .events(Collections.singletonList(Event.builder()
                .value(EventDetails.builder()
                    .type(APPEAL_RECEIVED.getType())
                    .date("2017-05-23T13:18:15.073")
                    .description("Appeal received")
                    .build())
                .build()))
            .build();

        given(updateCcdCaseData.updateCcdRecordForChangesAndReturnUpdateType(any(), any()))
            .willReturn(UpdateType.DATA_UPDATE);

        SscsCaseDetails existingCcdCase = SscsCaseDetails.builder()
            .id(1L)
            .data(SscsCaseData.builder().build())
            .build();

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCcdCase, idamTokens);

        verify(updateCcdCaseService).updateCaseV2(
            eq(existingCcdCase.getId()),
            eq("caseUpdated"),
            eq(SSCS_APPEAL_UPDATED_EVENT),
            eq(UPDATED_SSCS),
            eq(idamTokens),
            caseDetailsCaptor.capture());

        caseDetailsCaptor.getValue().accept(existingCcdCase);

        verify(updateCcdCaseData, times(2))
            .updateCcdRecordForChangesAndReturnUpdateType(
                caseData,
                existingCcdCase.getData());

        verify(updateCcdCaseService, never())
            .updateCase(any(SscsCaseData.class), anyLong(), eq("caseUpdated"),
                eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));
    }

    @Test
    public void shouldNotUpdateCcdGivenThereIsNoEventChangeOrDataChange() throws Exception {
        SscsCaseData caseData = buildTestCaseDataWithAppellantAndBenefitType();

        given(updateCcdCaseData.updateCcdRecordForChangesAndReturnUpdateType(any(), any()))
            .willReturn(UpdateType.NO_UPDATE);

        ccdCasesSender.sendUpdateCcdCases(caseData,
            getSscsCaseDetails(CASE_DETAILS_WITH_APPEAL_RECEIVED_JSON), idamTokens);

        verify(updateCcdCaseService, times(0))
            .updateCase(eq(caseData), anyLong(), any(),
                eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));
    }

    @Test
    void shouldNotUpdateCcdGivenThereIsNoEventChangeOrDataChangeV2() throws Exception {
        setField(ccdCasesSender, "updateCaseV2Enabled", true);

        SscsCaseData caseData = buildTestCaseDataWithAppellantAndBenefitType();

        given(updateCcdCaseData.updateCcdRecordForChangesAndReturnUpdateType(any(), any()))
            .willReturn(UpdateType.NO_UPDATE);
        SscsCaseDetails sscsCaseDetails = getSscsCaseDetails(CASE_DETAILS_WITH_APPEAL_RECEIVED_JSON);
        ccdCasesSender.sendUpdateCcdCases(caseData,
            sscsCaseDetails, idamTokens);

        verify(updateCcdCaseService, never()).updateCaseV2(
            anyLong(),
            any(),
            eq(SSCS_APPEAL_UPDATED_EVENT),
            eq(UPDATED_SSCS),
            eq(idamTokens),
            caseDetailsCaptor.capture());

        verify(updateCcdCaseData, times(1))
            .updateCcdRecordForChangesAndReturnUpdateType(caseData, sscsCaseDetails.getData());

        verify(updateCcdCaseService, never())
            .updateCase(eq(caseData), anyLong(), any(),
                eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));
    }

    @Test
    public void shouldNotUpdateCcdGivenNewEventIsNull() throws Exception {
        SscsCaseData caseData = SscsCaseData.builder().build();

        ccdCasesSender.sendUpdateCcdCases(caseData, getSscsCaseDetails(CASE_DETAILS_JSON), idamTokens);

        verify(updateCcdCaseService, times(0))
            .updateCase(eq(caseData), anyLong(), any(),
                eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));
    }

    @Test
    void shouldNotUpdateCcdGivenNewEventIsNullV2() throws Exception {
        setField(ccdCasesSender, "updateCaseV2Enabled", true);
        SscsCaseData caseData = SscsCaseData.builder().build();

        SscsCaseDetails sscsCaseDetails = getSscsCaseDetails(CASE_DETAILS_JSON);
        ccdCasesSender.sendUpdateCcdCases(caseData, sscsCaseDetails, idamTokens);

        verify(updateCcdCaseService, never()).updateCaseV2(
            anyLong(),
            any(),
            eq(SSCS_APPEAL_UPDATED_EVENT),
            eq(UPDATED_SSCS),
            eq(idamTokens),
            caseDetailsCaptor.capture());

        verify(updateCcdCaseData, never())
            .updateCcdRecordForChangesAndReturnUpdateType(any(), any());

        verify(updateCcdCaseService, never())
            .updateCase(eq(caseData), anyLong(), any(),
                eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));
    }

    @Test
    public void shouldNotUpdateCcdGivenNoNewFurtherEvidenceReceived() throws Exception {
        SscsCaseData caseData = buildTestCaseDataWithEventAndEvidence();
        caseData.setAppeal(buildAppeal());

        SscsCaseDetails existingCaseDetails = getSscsCaseDetails(CASE_DETAILS_WITH_ONE_EVIDENCE_AND_ONE_EVENT_JSON);

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails, idamTokens);

        verify(updateCcdCaseService, times(0))
            .updateCase(any(SscsCaseData.class), anyLong(), eq("evidenceReceived"),
                eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));
    }

    @Test
    void shouldNotUpdateCcdGivenNoNewFurtherEvidenceReceivedV2() throws Exception {
        setField(ccdCasesSender, "updateCaseV2Enabled", true);

        SscsCaseData caseData = buildTestCaseDataWithEventAndEvidence();
        caseData.setAppeal(buildAppeal());

        SscsCaseDetails existingCaseDetails = getSscsCaseDetails(CASE_DETAILS_WITH_ONE_EVIDENCE_AND_ONE_EVENT_JSON);

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails, idamTokens);

        verify(updateCcdCaseService, never()).updateCaseV2(
            anyLong(),
            eq("evidenceReceived"),
            eq(SSCS_APPEAL_UPDATED_EVENT),
            eq(UPDATED_SSCS),
            eq(idamTokens),
            caseDetailsCaptor.capture());

        verify(updateCcdCaseData, times(1))
            .updateCcdRecordForChangesAndReturnUpdateType(caseData, existingCaseDetails.getData());

        verify(updateCcdCaseService, never())
            .updateCase(any(SscsCaseData.class), anyLong(), eq("evidenceReceived"),
                eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));
    }

    @Test
    public void shouldNotUpdateCcdGivenNoNewFurtherEvidenceReceivedAndExisingEvidenceIsNull() throws Exception {
        SscsCaseData caseData = SscsCaseData.builder()
                .evidence(Evidence.builder()
                        .documents(Collections.emptyList())
                        .build())
                .events(Collections.singletonList(Event.builder()
                        .value(EventDetails.builder()
                                .type(APPEAL_RECEIVED.getType())
                                .date("2017-05-23T13:18:15.073")
                                .description("Appeal received")
                                .build())
                        .build()))
                .build();
        caseData.setAppeal(buildAppeal());

        SscsCaseDetails existingCaseDetails = getSscsCaseDetails(CASE_DETAILS_JSON);
        existingCaseDetails.getData().setEvidence(null);

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails, idamTokens);

        verify(updateCcdCaseService, times(0))
                .updateCase(any(SscsCaseData.class), anyLong(), eq("evidenceReceived"),
                        eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));
    }

    @Test
    void shouldNotUpdateCcdGivenNoNewFurtherEvidenceReceivedAndExisingEvidenceIsNullV2() throws Exception {
        setField(ccdCasesSender, "updateCaseV2Enabled", true);

        SscsCaseData caseData = SscsCaseData.builder()
                .evidence(Evidence.builder()
                        .documents(Collections.emptyList())
                        .build())
                .events(Collections.singletonList(Event.builder()
                        .value(EventDetails.builder()
                                .type(APPEAL_RECEIVED.getType())
                                .date("2017-05-23T13:18:15.073")
                                .description("Appeal received")
                                .build())
                        .build()))
                .build();
        caseData.setAppeal(buildAppeal());

        SscsCaseDetails existingCaseDetails = getSscsCaseDetails(CASE_DETAILS_JSON);
        existingCaseDetails.getData().setEvidence(null);

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails, idamTokens);

        verify(updateCcdCaseService, never()).updateCaseV2(
            anyLong(),
            eq("evidenceReceived"),
            eq(SSCS_APPEAL_UPDATED_EVENT),
            eq(UPDATED_SSCS),
            eq(idamTokens),
            caseDetailsCaptor.capture());

        verify(updateCcdCaseData, times(1))
            .updateCcdRecordForChangesAndReturnUpdateType(caseData, existingCaseDetails.getData());

        verify(updateCcdCaseService, never())
                .updateCase(any(SscsCaseData.class), anyLong(), eq("evidenceReceived"),
                        eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));
    }

    public static SscsCaseData buildTestCaseDataWithEventAndEvidence() {
        return SscsCaseData.builder()
            .evidence(Evidence.builder()
                .documents(Collections.singletonList(Document.builder()
                    .value(DocumentDetails.builder()
                        .dateReceived("2017-05-24")
                        .evidenceType("Medical evidence")
                        .evidenceProvidedBy("Appellant")
                        .build())
                    .build()))
                .build())
            .events(Collections.singletonList(Event.builder()
                .value(EventDetails.builder()
                    .type(APPEAL_RECEIVED.getType())
                    .date("2017-05-23T13:18:15.073")
                    .description("Appeal received")
                    .build())
                .build()))
            .build();
    }

    @Test
    public void shouldNotUpdateEvidenceIfEvidenceIsUnchanged() throws Exception {
        final SscsCaseData caseData = SscsCaseData.builder()
            .evidence(buildEvidence("2017-05-24"))
            .events(Collections.singletonList(Event.builder()
                .value(EventDetails.builder()
                    .type(APPEAL_RECEIVED.getType())
                    .date("2017-05-23T13:18:15.073")
                    .description("Appeal received")
                    .build())
                .build()))
            .appeal(buildAppeal())
            .build();

        final SscsCaseDetails existingCaseDetails = getSscsCaseDetails(CASE_DETAILS_JSON);
        existingCaseDetails.getData().setEvidence(caseData.getEvidence());

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails, idamTokens);

        verify(updateCcdCaseService, times(1))
            .updateCase(any(SscsCaseData.class), anyLong(), eq("appealReceived"),
                eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));

        verifyNoMoreInteractions(updateCcdCaseService);
    }

    @Test
    void shouldNotUpdateEvidenceIfEvidenceIsUnchangedV2() throws Exception {
        setField(ccdCasesSender, "updateCaseV2Enabled", true);

        final SscsCaseData caseData = SscsCaseData.builder()
            .evidence(buildEvidence("2017-05-24"))
            .events(Collections.singletonList(Event.builder()
                .value(EventDetails.builder()
                    .type(APPEAL_RECEIVED.getType())
                    .date("2017-05-23T13:18:15.073")
                    .description("Appeal received")
                    .build())
                .build()))
            .appeal(buildAppeal())
            .build();

        final SscsCaseDetails existingCaseDetails = getSscsCaseDetails(CASE_DETAILS_JSON);
        existingCaseDetails.getData().setEvidence(caseData.getEvidence());

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails, idamTokens);

        verify(updateCcdCaseService, times(1)).updateCaseV2(
            eq(existingCaseDetails.getId()),
            eq("appealReceived"),
            eq(SSCS_APPEAL_UPDATED_EVENT),
            eq(UPDATED_SSCS),
            eq(idamTokens),
            caseDetailsCaptor.capture());

        caseDetailsCaptor.getValue().accept(existingCaseDetails);

        verify(updateCcdCaseData, times(2))
            .updateCcdRecordForChangesAndReturnUpdateType(caseData, existingCaseDetails.getData());

        verify(updateCcdCaseService, never())
            .updateCase(any(SscsCaseData.class), anyLong(), eq("appealReceived"),
                eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));

        verifyNoMoreInteractions(updateCcdCaseService);
    }

    @Test
    public void shouldAddExistingHearingDetailsToTheCaseIfItsMissingInComingGaps2Xml() throws Exception {

        SscsCaseData caseData = buildCaseData(RESPONSE_RECEIVED);
        caseData.setHearings(buildHearings());
        ArgumentCaptor<SscsCaseData> caseDataArgumentCaptor = ArgumentCaptor.forClass(SscsCaseData.class);

        SscsCaseDetails existingCaseDetails = getSscsCaseDetails(CASE_DETAILS_WITH_HEARINGS_JSON);
        given(updateCcdCaseData.updateCcdRecordForChangesAndReturnUpdateType(any(), any()))
            .willReturn(UpdateType.EVENT_UPDATE);

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails, idamTokens);

        verify(updateCcdCaseService).updateCase(caseDataArgumentCaptor.capture(),
            eq(existingCaseDetails.getId()), eq(caseData.getLatestEventType()),
            eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));

        assertThat(caseDataArgumentCaptor.getValue().getHearings().size(), equalTo(3));

        HearingDetails hearingDetails = caseDataArgumentCaptor.getValue().getHearings().get(0).getValue();
        HearingDetails hearingDetails1 = caseDataArgumentCaptor.getValue().getHearings().get(1).getValue();

        assertThat(hearingDetails.getHearingDate() + hearingDetails.getTime(), equalTo("2018-04-0510:00:00"));
        assertThat(hearingDetails1.getHearingDate() + hearingDetails1.getTime(),
            equalTo("2017-05-2410:00"));
    }

    @Test
    void shouldAddExistingHearingDetailsToTheCaseIfItsMissingInComingGaps2XmlV2() throws Exception {
        setField(ccdCasesSender, "updateCaseV2Enabled", true);

        SscsCaseData caseData = buildCaseData(RESPONSE_RECEIVED);
        caseData.setHearings(buildHearings());

        SscsCaseDetails existingCaseDetails = getSscsCaseDetails(CASE_DETAILS_WITH_HEARINGS_JSON);
        given(updateCcdCaseData.updateCcdRecordForChangesAndReturnUpdateType(any(), any()))
            .willReturn(UpdateType.EVENT_UPDATE);

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails, idamTokens);

        verify(updateCcdCaseService).updateCaseV2(
            eq(existingCaseDetails.getId()),
            eq(caseData.getLatestEventType()),
            eq(SSCS_APPEAL_UPDATED_EVENT),
            eq(UPDATED_SSCS),
            eq(idamTokens),
            caseDetailsCaptor.capture());

        caseDetailsCaptor.getValue().accept(existingCaseDetails);

        verify(updateCcdCaseData, times(2))
            .updateCcdRecordForChangesAndReturnUpdateType(
                caseData,
                existingCaseDetails.getData());

        verify(updateCcdCaseService, never()).updateCase(any(),
            eq(existingCaseDetails.getId()), eq(caseData.getLatestEventType()),
            eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));

        assertThat(existingCaseDetails.getData().getCaseReference(), equalTo(caseData.getCaseReference()));
        assertThat(existingCaseDetails.getData().getHearings().size(), equalTo(3));

        HearingDetails hearingDetails = existingCaseDetails.getData().getHearings().get(0).getValue();
        HearingDetails hearingDetails1 = existingCaseDetails.getData().getHearings().get(1).getValue();

        assertThat(hearingDetails.getHearingDate() + hearingDetails.getTime(), equalTo("2018-04-0510:00:00"));
        assertThat(hearingDetails1.getHearingDate() + hearingDetails1.getTime(),
            equalTo("2017-05-2410:00"));
    }

    @Test
    public void shouldAddNewHearingDetailsFromGap2XmlToTheCcd() throws Exception {

        SscsCaseData caseData = buildCaseData(RESPONSE_RECEIVED);
        caseData.setHearings(buildHearings());
        ArgumentCaptor<SscsCaseData> caseDataArgumentCaptor = ArgumentCaptor.forClass(SscsCaseData.class);

        SscsCaseDetails existingCaseDetails = getSscsCaseDetails(CASE_DETAILS_WITH_NO_HEARINGS_JSON);

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails, idamTokens);

        verify(updateCcdCaseService).updateCase(caseDataArgumentCaptor.capture(),
            eq(existingCaseDetails.getId()), eq(caseData.getLatestEventType()),
            eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));

        HearingDetails hearingDetails = caseDataArgumentCaptor.getValue().getHearings().get(0).getValue();

        assertThat(caseDataArgumentCaptor.getValue().getHearings().size(), equalTo(1));
        assertThat(hearingDetails.getHearingDate() + hearingDetails.getTime(),
            equalTo("2018-04-0510:00:00"));
    }

    @Test
    void shouldAddNewHearingDetailsFromGap2XmlToTheCcdV2() throws Exception {
        setField(ccdCasesSender, "updateCaseV2Enabled", true);

        SscsCaseData caseData = buildCaseData(RESPONSE_RECEIVED);
        caseData.setHearings(buildHearings());

        SscsCaseDetails existingCaseDetails = getSscsCaseDetails(CASE_DETAILS_WITH_NO_HEARINGS_JSON);

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails, idamTokens);

        verify(updateCcdCaseService).updateCaseV2(
            eq(existingCaseDetails.getId()),
            eq(caseData.getLatestEventType()),
            eq(SSCS_APPEAL_UPDATED_EVENT),
            eq(UPDATED_SSCS),
            eq(idamTokens),
            caseDetailsCaptor.capture());

        caseDetailsCaptor.getValue().accept(existingCaseDetails);

        verify(updateCcdCaseData, times(2))
            .updateCcdRecordForChangesAndReturnUpdateType(
                caseData,
                existingCaseDetails.getData());

        verify(updateCcdCaseService, never()).updateCase(any(),
            eq(existingCaseDetails.getId()), eq(caseData.getLatestEventType()),
            eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));

        HearingDetails hearingDetails = existingCaseDetails.getData().getHearings().get(0).getValue();

        assertThat(existingCaseDetails.getData().getHearings().size(), equalTo(1));
        assertThat(hearingDetails.getHearingDate() + hearingDetails.getTime(),
            equalTo("2018-04-0510:00:00"));
    }

    @Test
    public void shouldAddExistingNewHearingDetailsFromCcdToCaseWhenNoHearingDetailsinGaps2Xml() throws Exception {

        SscsCaseData caseData = buildCaseData(RESPONSE_RECEIVED);
        ArgumentCaptor<SscsCaseData> caseDataArgumentCaptor = ArgumentCaptor.forClass(SscsCaseData.class);

        SscsCaseDetails existingCaseDetails = getSscsCaseDetails(CASE_DETAILS_WITH_HEARINGS_JSON);

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails, idamTokens);

        verify(updateCcdCaseService).updateCase(caseDataArgumentCaptor.capture(),
            eq(existingCaseDetails.getId()), eq(caseData.getLatestEventType()),
            eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));

        HearingDetails hearingDetails = caseDataArgumentCaptor.getValue().getHearings().get(0).getValue();

        assertThat(caseDataArgumentCaptor.getValue().getHearings().size(), equalTo(2));
        assertThat(hearingDetails.getHearingDate() + hearingDetails.getTime(),
            equalTo("2017-05-2410:00"));
    }

    @Test
    void shouldAddExistingNewHearingDetailsFromCcdToCaseWhenNoHearingDetailsinGaps2XmlV2() throws Exception {
        setField(ccdCasesSender, "updateCaseV2Enabled", true);

        SscsCaseData caseData = buildCaseData(RESPONSE_RECEIVED);

        SscsCaseDetails existingCaseDetails = getSscsCaseDetails(CASE_DETAILS_WITH_HEARINGS_JSON);

        ccdCasesSender.sendUpdateCcdCases(caseData, existingCaseDetails, idamTokens);

        verify(updateCcdCaseService).updateCaseV2(
            eq(existingCaseDetails.getId()),
            eq(caseData.getLatestEventType()),
            eq(SSCS_APPEAL_UPDATED_EVENT),
            eq(UPDATED_SSCS),
            eq(idamTokens),
            caseDetailsCaptor.capture());

        caseDetailsCaptor.getValue().accept(existingCaseDetails);

        verify(updateCcdCaseData, times(2))
            .updateCcdRecordForChangesAndReturnUpdateType(
                caseData,
                existingCaseDetails.getData());

        verify(updateCcdCaseService, never()).updateCase(any(),
            eq(existingCaseDetails.getId()), eq(caseData.getLatestEventType()),
            eq(SSCS_APPEAL_UPDATED_EVENT), eq(UPDATED_SSCS), eq(idamTokens));

        HearingDetails hearingDetails = existingCaseDetails.getData().getHearings().get(0).getValue();

        assertThat(existingCaseDetails.getData().getHearings().size(), equalTo(2));
        assertThat(hearingDetails.getHearingDate() + hearingDetails.getTime(),
            equalTo("2017-05-2410:00"));
    }

    private List<Hearing> buildHearings() {
        return Collections.singletonList(
            Hearing.builder().value(
                HearingDetails.builder().hearingDate("2018-04-05").time("10:00:00").build())
                .build());
    }

    public static SscsCaseData buildCaseData(GapsEvent event) {
        final Appeal appeal = buildAppeal();
        EventDetails appealCreatedEvent = EventDetails.builder()
            .type("appealCreated")
            .description("Appeal Created")
            .date("2018-01-14T21:59:43.10")
            .build();

        EventDetails updateEvent = EventDetails.builder()
            .type(event.getType())
            .description(event.getDescription())
            .date("2018-01-15T21:59:43.10")
            .build();

        List<Event> events = new ArrayList<>();

        events.add(Event.builder().value(appealCreatedEvent).build());
        events.add(Event.builder().value(updateEvent).build());

        events.sort(Collections.reverseOrder());

        return SscsCaseData.builder()
            .caseReference("SC068/17/00011")
            .appeal(appeal)
            .events(events)
            .build();
    }

    public static Appeal buildAppeal() {
        Name name = Name.builder()
            .title("Mr")
            .firstName("User")
            .lastName("Test")
            .build();
        Contact contact = Contact.builder()
            .email("mail@email.com")
            .phone("01234567890")
            .mobile("01234567890")
            .build();
        Identity identity = Identity.builder()
            .dob("1904-03-10")
            .nino("AB 22 55 66 B")
            .build();
        Appellant appellant = Appellant.builder()
            .name(name)
            .contact(contact)
            .identity(identity)
            .build();
        BenefitType benefitType = BenefitType.builder()
            .code("PIP")
            .build();

        return Appeal.builder()
            .appellant(appellant)
            .benefitType(benefitType)
            .build();
    }

    private Evidence buildEvidence(String receivedDate) {
        DocumentDetails document1 = DocumentDetails.builder()
            .evidenceType("Medical evidence")
            .evidenceProvidedBy("Appellant")
            .dateReceived(receivedDate)
            .build();

        DocumentDetails document2 = DocumentDetails.builder()
            .evidenceType("Medical evidence")
            .evidenceProvidedBy("Appellant")
            .dateReceived("2017-05-25")
            .build();

        List<Document> documents = new ArrayList<>();

        documents.add(Document.builder().value(document1).build());
        documents.add(Document.builder().value(document2).build());

        return Evidence.builder().documents(documents).build();

    }

    public static SscsCaseData buildTestCaseDataWithAppellantAndBenefitType() {
        final Appeal appeal = buildAppeal();

        EventDetails event = EventDetails.builder()
            .type("appealReceived")
            .description("Appeal Received")
            .date("2001-12-14T21:59:43.10-05:00")
            .build();
        Event events = Event.builder()
            .value(event)
            .build();

        return SscsCaseData.builder()
            .appeal(appeal)
            .events(Collections.singletonList(events))
            .build();
    }

}
