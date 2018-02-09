package uk.gov.hmcts.reform.sscs.models.deserialize.gaps2;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;

@Value
public class PostponementRequests {
    private String postponementGranted;
    private String appealHearingId;
    private String postponementReasonId;
    private String roleRequestedById;

    public PostponementRequests(@JsonProperty("Postponement_Granted") String postponementGranted,
                       @JsonProperty("Appeal_Hearing_Id") String appealHearingId,
                       @JsonProperty("Postponement_Reason_Id") String postponementReasonId,
                       @JsonProperty("Role_Requested_By_Id") String roleRequestedById) {
        this.postponementGranted = postponementGranted;
        this.appealHearingId = appealHearingId;
        this.postponementReasonId = postponementReasonId;
        this.roleRequestedById = roleRequestedById;
    }
}
