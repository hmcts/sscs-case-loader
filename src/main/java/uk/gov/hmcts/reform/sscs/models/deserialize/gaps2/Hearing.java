package uk.gov.hmcts.reform.sscs.models.deserialize.gaps2;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Hearing {

    String outcomeId;
    String venueId;
    String dateOutcomeDecisionNotification;
    String dateHearingNotification;
    String sessionDate;
    String appealTime;
    String hearingId;

    public Hearing(@JsonAlias("Outcome_Id") String outcomeId,
                   @JsonAlias("Venue_Id") String venueId,
                   @JsonAlias("Date_Outcome_Decision_Notification") String dateOutcomeDecisionNotification,
                   @JsonAlias("Date_Hearing_Notification") String dateHearingNotification,
                   @JsonAlias("Session_Date") String sessionDate,
                   @JsonAlias("Appeal_Time") String appealTime,
                   @JsonAlias("Hearing_Id") String hearingId) {
        this.outcomeId = outcomeId;
        this.venueId = venueId;
        this.dateOutcomeDecisionNotification = dateOutcomeDecisionNotification;
        this.dateHearingNotification = dateHearingNotification;
        this.sessionDate = sessionDate;
        this.appealTime = appealTime;
        this.hearingId = hearingId;
    }
}
