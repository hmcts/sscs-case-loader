package uk.gov.hmcts.reform.sscs.services.mapper;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.models.GapsEvent;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.AppealCase;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.MajorStatus;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.MinorStatus;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Event;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Events;

@Service
public class CaseDataEventBuilder {

    public List<Events> buildPostponedEvent(AppealCase appealCase) {
        if (minorStatusIsNotNullAndIsNotEmpty(appealCase.getMinorStatus())
            && minorStatusIdIsEqualTo26(appealCase) && postponedEventIsNotPresentAlready(appealCase)) {
            return buildNewPostponedEvent(appealCase);
        }
        return Collections.emptyList();
    }

    private List<Events> buildNewPostponedEvent(AppealCase appealCase) {
        return Collections.singletonList(Events.builder()
            .value(Event.builder()
                .type(GapsEvent.HEARING_POSTPONED.getType())
                .date(appealCase.getMinorStatus().get(0).getDateSet().toLocalDateTime().toString())
                .description(GapsEvent.HEARING_POSTPONED.getDescription())
                .build())
            .build());
    }

    private boolean postponedEventIsNotPresentAlready(AppealCase appealCase) {
        ZonedDateTime minorStatusDate = appealCase.getMinorStatus().get(0).getDateSet();
        return appealCase.getMajorStatus()
            .stream()
            .filter(majorStatus -> majorStatus.getStatusId().equals(GapsEvent.HEARING_POSTPONED.getStatus()))
            .filter(majorStatus -> majorStatus.getDateSet().equals(minorStatusDate)).collect(Collectors.toList())
            .isEmpty();
    }

    private boolean minorStatusIdIsEqualTo26(AppealCase appealCase) {
        return "26".equals(appealCase.getMinorStatus().get(0).getStatusId());
    }

    private boolean minorStatusIsNotNullAndIsNotEmpty(List<MinorStatus> minorStatusList) {
        return minorStatusList != null && !minorStatusList.isEmpty();
    }

    public List<Events> buildMajorStatusEvents(AppealCase appealCase) {
        List<Events> events = new ArrayList<>();
        for (MajorStatus majorStatus : appealCase.getMajorStatus()) {
            GapsEvent gapsEvent = GapsEvent.getGapsEventByStatus(majorStatus.getStatusId());
            if (gapsEvent != null) {
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


    public List<Events> buildAdjournedEvents(AppealCase appealCase) {
        List<Events> events = new ArrayList<>();

        appealCase.getHearing().stream()
            .forEach(hearing -> {
                int outcomeId = Integer.parseInt(hearing.getOutcomeId());
                    if (outcomeId >= 110 && outcomeId <= 126) {
                    Event adjournedEvent = Event.builder()
                        .type(GapsEvent.HEARING_ADJOURNED.getType())
                        .date(hearing.getSessionDate())
                        .description(GapsEvent.HEARING_ADJOURNED.getDescription())
                        .build();

                    events.add(Events.builder()
                        .value(adjournedEvent)
                        .build());
                    }
                }
        );
        return  events;
    }
}
