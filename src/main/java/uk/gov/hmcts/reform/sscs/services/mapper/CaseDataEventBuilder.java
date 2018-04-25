package uk.gov.hmcts.reform.sscs.services.mapper;

import java.util.ArrayList;
import java.util.List;
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
        List<Events> events = new ArrayList<>();
        if (minorStatusIsNotNullAndIsNotEmpty(appealCase.getMinorStatus())) {
            events.add(Events.builder()
                .value(Event.builder()
                    .type(GapsEvent.HEARING_POSTPONED.getType())
                    .date(appealCase.getMinorStatus().get(0).getDateSet().toLocalDateTime().toString())
                    .description(GapsEvent.HEARING_POSTPONED.getDescription())
                    .build())
                .build());
        }
        return events;
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



}
