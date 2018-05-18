package uk.gov.hmcts.reform.sscs.services.mapper;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.models.GapsEvent;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.AppealCase;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.MajorStatus;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.MinorStatus;
import uk.gov.hmcts.reform.sscs.models.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Event;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Events;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Hearing;
import uk.gov.hmcts.reform.sscs.services.ccd.SearchCcdService;
import uk.gov.hmcts.reform.sscs.services.idam.IdamService;
import uk.gov.hmcts.reform.sscs.util.CcdUtil;

@Service
class CaseDataEventBuilder {

    private final SearchCcdService searchCcdService;
    private final IdamService idamService;
    private final PostponedEventService<uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Hearing>
        postponedEventInferredFromDelta;
    private final PostponedEventService<Hearing> postponedEventInferredFromCcd;

    @Autowired
    CaseDataEventBuilder(
        SearchCcdService searchCcdService, IdamService idamService,
        PostponedEventService<uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Hearing>
            postponedEventInferredFromDelta,
        PostponedEventService<Hearing> postponedEventInferredFromCcd) {
        this.searchCcdService = searchCcdService;
        this.idamService = idamService;
        this.postponedEventInferredFromDelta = postponedEventInferredFromDelta;
        this.postponedEventInferredFromCcd = postponedEventInferredFromCcd;
    }

    List<Events> buildPostponedEvent(AppealCase appealCase) {
        List<Events> events = new ArrayList<>();
        events.addAll(buildPostponedEventsFromMajorStatus(appealCase));
        events.addAll(buildPostponedEventsFromMinorStatus(appealCase));
        events.addAll(buildPostponedEventsFromHearingOutcomeId(appealCase));
        return events;
    }

    private List<Events> buildPostponedEventsFromHearingOutcomeId(AppealCase appealCase) {
        if (hearingExists(appealCase)) {
            return getEventsByHearingOutcomeId(appealCase, 12, 16,
                GapsEvent.HEARING_POSTPONED);
        }
        return Collections.emptyList();
    }

    private List<Events> buildPostponedEventsFromMinorStatus(AppealCase appealCase) {
        if (minorStatusIsNotNullAndIsNotEmpty(appealCase.getMinorStatus())) {
            return appealCase.getMinorStatus().stream()
                .filter(minorStatus -> areConditionsToCreatePostponedEventMet(minorStatus.getStatusId(), appealCase))
                .map(minorStatus -> buildNewPostponedEvent(minorStatus.getDateSet()))
                .distinct()
                .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    private List<Events> buildPostponedEventsFromMajorStatus(AppealCase appealCase) {
        MajorStatus latestMajorStatus = getLatestMajorStatusFromAppealCase(appealCase.getMajorStatus());
        if (areConditionsFromMajorStatusToCreatePostponedMet(appealCase, latestMajorStatus)) {
            return Collections.singletonList(buildNewPostponedEvent(latestMajorStatus.getDateSet()));
        }
        return Collections.emptyList();
    }

    private MajorStatus getLatestMajorStatusFromAppealCase(List<MajorStatus> majorStatus) {
        return Collections.max(majorStatus, Comparator.comparing(MajorStatus::getDateSet));
    }

    private boolean areConditionsFromMajorStatusToCreatePostponedMet(AppealCase appealCase,
                                                                     MajorStatus latestMajorStatus) {

        return isResponseReceivedTheAppealCurrentStatus(latestMajorStatus) && isPostponementGranted(appealCase)
            && postponedEventInferredFromCcd.matchToHearingId(appealCase.getPostponementRequests(),
            retrieveHearingsFromCaseInCcd(appealCase));
    }

    private boolean isResponseReceivedTheAppealCurrentStatus(MajorStatus latestMajorStatus) {
        return "18".equals(latestMajorStatus.getStatusId());
    }

    private boolean isPostponementGranted(AppealCase appealCase) {
        if (appealCase.getPostponementRequests() != null) {
            return !appealCase.getPostponementRequests().stream()
                .filter(postponementRequests -> "Y".equals(postponementRequests.getPostponementGranted()))
                .collect(Collectors.toList())
                .isEmpty();
        }
        return false;
    }

    private boolean areConditionsToCreatePostponedEventMet(String statusId, AppealCase appealCase) {
        if (minorStatusIdIs27AndThereIsOnlyOnePostponementRequestGranted(statusId, appealCase)) {
            return true;
        }

        if (minorStatusIdIs27AndMoreThanOnePostponementRequest(statusId, appealCase)) {
            if (appealCase.getHearing() != null && !appealCase.getHearing().isEmpty()
                && postponedEventInferredFromDelta.matchToHearingId(appealCase.getPostponementRequests(),
                appealCase.getHearing())) {
                return true;

            }
            return postponedEventInferredFromCcd.matchToHearingId(appealCase.getPostponementRequests(),
                retrieveHearingsFromCaseInCcd(appealCase));
        }
        return false;
    }

    @Retryable
    private List<Hearing> retrieveHearingsFromCaseInCcd(AppealCase appealCase) {
        String serviceAuthorization = idamService.generateServiceAuthorization();
        IdamTokens idamTokens = IdamTokens.builder()
            .idamOauth2Token(idamService.getIdamOauth2Token())
            .authenticationService(serviceAuthorization)
            .serviceUserId(idamService.getServiceUserId(serviceAuthorization))
            .build();
        List<CaseDetails> caseDetailsList = searchCcdService.findCaseByCaseRef(appealCase.getAppealCaseRefNum(),
            idamTokens);
        if (caseDetailsList != null && !caseDetailsList.isEmpty()) {
            return CcdUtil.getCaseData(caseDetailsList.get(0).getData()).getHearings();
        }
        return Collections.emptyList();
    }

    private boolean minorStatusIdIs27AndMoreThanOnePostponementRequest(String statusId, AppealCase appealCase) {
        return "27".equals(statusId) && appealCase.getPostponementRequests() != null
            && !appealCase.getPostponementRequests().isEmpty() && appealCase.getPostponementRequests().size() > 1;
    }

    private boolean minorStatusIdIs27AndThereIsOnlyOnePostponementRequestGranted(String statusId,
                                                                                 AppealCase appealCase) {
        return "27".equals(statusId) && appealCase.getPostponementRequests() != null
            && !appealCase.getPostponementRequests().isEmpty()
            && appealCase.getPostponementRequests().size() == 1
            && "Y".equals(appealCase.getPostponementRequests().get(0).getPostponementGranted());
    }

    private Events buildNewPostponedEvent(ZonedDateTime dateSet) {
        return Events.builder()
            .value(Event.builder()
                .type(GapsEvent.HEARING_POSTPONED.getType())
                .date(dateSet.toLocalDateTime().toString())
                .description(GapsEvent.HEARING_POSTPONED.getDescription())
                .build())
            .build();
    }

    private boolean minorStatusIsNotNullAndIsNotEmpty(List<MinorStatus> minorStatusList) {
        return minorStatusList != null && !minorStatusList.isEmpty();
    }

    List<Events> buildMajorStatusEvents(AppealCase appealCase) {
        List<Events> events = new ArrayList<>();
        List<MajorStatus> majorStatusList = appealCase.getMajorStatus();
        Collections.sort(majorStatusList);
        for (MajorStatus majorStatus : majorStatusList) {
            GapsEvent gapsEvent = GapsEvent.getGapsEventByStatus(majorStatus.getStatusId());
            if (gapsEvent != null && !hearingPostponed(gapsEvent)) {
                Event event = Event.builder()
                    .type(gapsEvent.getType())
                    .description(gapsEvent.getDescription())
                    .date(majorStatus.getDateSet().toLocalDateTime().toString())
                    .build();
                boolean responseReceivedEventAlreadyPresent = events.stream()
                    .anyMatch(e -> e.getValue().getType().equals(GapsEvent.RESPONSE_RECEIVED.getType()));

                if (!(event.getType().equals(GapsEvent.RESPONSE_RECEIVED.getType())
                    && responseReceivedEventAlreadyPresent)) {
                    events.add(Events.builder()
                        .value(event)
                        .build());
                }
            }
        }
        return events;
    }

    List<Events> buildAdjournedEvents(AppealCase appealCase) {

        if (hearingExists(appealCase)) {
            return getEventsByHearingOutcomeId(appealCase, 110, 126, GapsEvent.HEARING_ADJOURNED);
        }
        return Collections.emptyList();
    }

    private List<Events> getEventsByHearingOutcomeId(AppealCase appealCase,
                                                     int rangeStart, int rangeEnd,
                                                     GapsEvent gapsEvent) {
        List<Events> events = new ArrayList<>();
        appealCase.getHearing().stream()
            .filter(hearing -> null != hearing.getOutcomeId())
            .forEach(hearing -> {
                int outcomeId = Integer.parseInt(hearing.getOutcomeId());
                if (outcomeId >= rangeStart && outcomeId <= rangeEnd) {
                    Event adjournedEvent = Event.builder()
                        .type(gapsEvent.getType())
                        .date(getLocalDateTime(hearing.getSessionDate()))
                        .description(gapsEvent.getDescription())
                        .build();

                    events.add(Events.builder()
                        .value(adjournedEvent)
                        .build());
                }
            });
        return events;
    }

    private boolean hearingPostponed(GapsEvent gapsEvent) {
        return gapsEvent.getStatus().equals("27");
    }

    private String getLocalDateTime(String zonedDateTimeWithOffset) {
        return ZonedDateTime
            .parse(zonedDateTimeWithOffset)
            .toLocalDateTime()
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private boolean hearingExists(AppealCase appealCase) {
        return null != appealCase.getHearing() && !appealCase.getHearing().isEmpty();
    }
}
