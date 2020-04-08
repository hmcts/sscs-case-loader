package uk.gov.hmcts.reform.sscs.models.deserialize.gaps2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class FurtherEvidence {
    private String furEvidenceId;
    private String feRoleId;
    private String feDateReceived;
    private String feDateIssued;
    private String feTypeofEvidenceId;

    public FurtherEvidence(@JsonProperty("FUR_EVIDENCE_ID") String furEvidenceId,
                           @JsonProperty("FE_Role_Id") String feRoleId,
                           @JsonProperty("FE_Date_Received") String feDateReceived,
                           @JsonProperty("FE_Date_Issued") String feDateIssued,
                           @JsonProperty("FE_Type_of_Evidence_Id") String feTypeofEvidenceId) {
        this.furEvidenceId = furEvidenceId;
        this.feRoleId = feRoleId;
        this.feDateReceived = feDateReceived;
        this.feDateIssued = feDateIssued;
        this.feTypeofEvidenceId = feTypeofEvidenceId;
    }
}
