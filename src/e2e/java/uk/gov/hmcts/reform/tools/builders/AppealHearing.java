package uk.gov.hmcts.reform.tools.builders;

import java.util.HashMap;
import java.util.Map;
import uk.gov.hmcts.reform.tools.enums.OutcomeType;
import uk.gov.hmcts.reform.tools.utils.XMLTags;

public class AppealHearing {

    private Map<String, String> appealMajorStatuses = new HashMap<>();

    public AppealHearing setHearingId(String value){
        appealMajorStatuses.put(XMLTags.hearing_Id, value);
        return this;
    }

    public AppealHearing setDateHearingNotification(String value){
        appealMajorStatuses.put(XMLTags.date_hearing_notification, value);
        return this;
    }

    public AppealHearing setDateOutcomeDescisionNotification(String value){
        appealMajorStatuses.put(XMLTags.date_outcome_decision_notification, value);
        return this;
    }

    public AppealHearing setSessionDate(String value){
        appealMajorStatuses.put(XMLTags.session_date, value);
        return this;
    }

    public AppealHearing setAppealTime(String value){
        appealMajorStatuses.put(XMLTags.appeal_time, value);
        return this;
    }

    public AppealHearing setVenueId(String value){
        appealMajorStatuses.put(XMLTags.venue_id, value);
        return this;
    }

    public AppealHearing setOutcome(OutcomeType value){
        appealMajorStatuses.put(XMLTags.outcome_id, value.toString());
        return this;
    }

    public Map<String, String> build() {
        return appealMajorStatuses;
    }

}
