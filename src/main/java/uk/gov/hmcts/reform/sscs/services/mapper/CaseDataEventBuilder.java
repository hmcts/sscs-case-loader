package uk.gov.hmcts.reform.sscs.services.mapper;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.models.GapsEvent;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.AppealCase;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Hearing;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.MajorStatus;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.MinorStatus;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.PostponementRequests;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Event;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Events;

@Service
class CaseDataEventBuilder {

    List<Events> buildPostponedEvent(AppealCase appealCase) {
        if (minorStatusIsNotNullAndIsNotEmpty(appealCase.getMinorStatus())) {
            return appealCase.getMinorStatus().stream()
                .filter(minorStatus -> areConditionsToCreatePostponedEventMet(minorStatus.getStatusId(), appealCase))
                .map(minorStatus -> buildNewPostponedEvent(minorStatus.getDateSet()))
                .distinct()
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private boolean areConditionsToCreatePostponedEventMet(String statusId, AppealCase appealCase) {
        if (minorStatusIdIs27AndThereIsOnlyOnePostponementRequest(statusId, appealCase)) {
            return true;
        }
        //todo call to CDD to get hearing id's from the exiting case in CDD.
        if (minorStatusIdIs27AndMoreThanOnePostponementRequest(statusId, appealCase)) {
            return !appealCase.getPostponementRequests().stream()
                .filter(postponementRequest -> "Y".equals(postponementRequest.getPostponementGranted()))
                .filter(postponementRequest -> matchToHearingId(postponementRequest, appealCase.getHearing()))
                .collect(Collectors.toList())
                .isEmpty();
        }
        return false;
    }

    private boolean matchToHearingId(PostponementRequests postponementRequest, List<Hearing> hearingList) {
        return !hearingList.stream()
            .filter(hearing -> hearing.getHearingId().equals(postponementRequest.getAppealHearingId()))
            .collect(Collectors.toList())
            .isEmpty();
    }

    private boolean minorStatusIdIs27AndMoreThanOnePostponementRequest(String statusId, AppealCase appealCase) {
        return "27".equals(statusId) && appealCase.getPostponementRequests() != null
            && !appealCase.getPostponementRequests().isEmpty();
    }

    private boolean minorStatusIdIs27AndThereIsOnlyOnePostponementRequest(String statusId, AppealCase appealCase) {
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
        for (MajorStatus majorStatus : appealCase.getMajorStatus()) {
            GapsEvent gapsEvent = GapsEvent.getGapsEventByStatus(majorStatus.getStatusId());
            if (gapsEvent != null && !hearingPostponed(gapsEvent)) {
                Event event = Event.builder()
                    .type(gapsEvent.getType())
                    .description(gapsEvent.getDescription())
                    .date(majorStatus.getDateSet().toLocalDateTime().toString())
                    .build();
                events.add(Events.builder()
                    .value(event)
                    .build());
            }
        }
        return events;
    }

    private boolean hearingPostponed(GapsEvent gapsEvent) {
        return gapsEvent.getStatus().equals("27");
    }

    public List<Events> buildAdjournedEvents(AppealCase appealCase) {
        if (null != appealCase.getHearing() && !appealCase.getHearing().isEmpty()) {
            List<Events> events = new ArrayList<>();
            appealCase.getHearing().stream()
                .filter(hearing -> null != hearing.getOutcomeId())
                .forEach(hearing -> {
                    int outcomeId = Integer.parseInt(hearing.getOutcomeId());
                    if (outcomeId >= 110 && outcomeId <= 126) {
                        Event adjournedEvent = Event.builder()
                            .type(GapsEvent.HEARING_ADJOURNED.getType())
                            .date(getLocalDateTime(hearing.getSessionDate()))
                            .description(GapsEvent.HEARING_ADJOURNED.getDescription())
                            .build();

                        events.add(Events.builder()
                            .value(adjournedEvent)
                            .build());
                    }
                });
            return events;
        }
        return Collections.emptyList();
    }

    private String getLocalDateTime(String zonedDateTimeWithOffset) {
        return ZonedDateTime
            .parse(zonedDateTimeWithOffset)
            .toLocalDateTime()
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
