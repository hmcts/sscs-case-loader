package uk.gov.hmcts.reform.sscs.models.deserialize.gaps2;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;
import lombok.Value;

@Value
public class Hearing {
    private String outcomeId;
    private String venueId;
    private String dateOutcomeDecisionNotification;
    private String dateHearingNotification;
    private ZonedDateTime sessionDate;
    private String appealTime;
    private String hearingId;

    public Hearing(@JsonProperty("Outcome_Id") String outcomeId,
                   @JsonProperty("Venue_Id") String venueId,
                   @JsonProperty("Date_Outcome_Decision_Notification") String dateOutcomeDecisionNotification,
                   @JsonProperty("Date_Hearing_Notification") String dateHearingNotification,
                   @JsonProperty("Session_Date") ZonedDateTime sessionDate,
                   @JsonProperty("Appeal_Time") String appealTime,
                   @JsonProperty("Hearing_Id") String hearingId) {
        this.outcomeId = outcomeId;
        this.venueId = venueId;
        this.dateOutcomeDecisionNotification = dateOutcomeDecisionNotification;
        this.dateHearingNotification = dateHearingNotification;
        this.sessionDate = sessionDate;
        this.appealTime = appealTime;
        this.hearingId = hearingId;
    }
}
