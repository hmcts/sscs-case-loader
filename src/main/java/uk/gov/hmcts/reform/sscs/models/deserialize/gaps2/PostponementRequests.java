package uk.gov.hmcts.reform.sscs.models.deserialize.gaps2;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Value;

@Value
public class PostponementRequests {

    String postponementGranted;
    String appealHearingId;
    String postponementReasonId;
    String roleRequestedById;

    public PostponementRequests(@JsonAlias("Postponement_Granted") String postponementGranted,
                                @JsonAlias("Appeal_Hearing_Id") String appealHearingId,
                                @JsonAlias("Postponement_Reason_Id") String postponementReasonId,
                                @JsonAlias("Role_Requested_By_Id") String roleRequestedById) {
        this.postponementGranted = postponementGranted;
        this.appealHearingId = appealHearingId;
        this.postponementReasonId = postponementReasonId;
        this.roleRequestedById = roleRequestedById;
    }
}
