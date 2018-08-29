package uk.gov.hmcts.reform.sscs.models.serialize.ccd;

import com.fasterxml.jackson.annotation.*;
import java.util.List;
import java.util.Objects;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.sscs.models.refdata.RegionalProcessingCenter;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.subscriptions.Subscriptions;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseData {
    private String caseReference;
    private Appeal appeal;
    private List<Hearing> hearings;
    private Evidence evidence;
    private List<DwpTimeExtension> dwpTimeExtension;
    private List<Events> events;
    private String generatedNino;
    private String generatedSurname;
    private String generatedEmail;
    private String generatedMobile;
    @JsonProperty("generatedDOB")
    private String generatedDob;
    private Subscriptions subscriptions;
    private RegionalProcessingCenter regionalProcessingCenter;
    private String region;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String ccdCaseId;

    @JsonIgnore
    private Event getLatestEvent() {
        return events != null && !events.isEmpty() ? events.get(0).getValue() : null;
    }

    @JsonIgnore
    public String getLatestEventType() {
        Event latestEvent = getLatestEvent();
        return latestEvent != null ? latestEvent.getType() : null;
    }

    @JsonCreator
    public CaseData(@JsonProperty("caseReference") String caseReference,
                    @JsonProperty("appeal") Appeal appeal,
                    @JsonProperty("hearings") List<Hearing> hearings,
                    @JsonProperty("evidence") Evidence evidence,
                    @JsonProperty("dwpTimeExtension") List<DwpTimeExtension> dwpTimeExtension,
                    @JsonProperty("events") List<Events> events,
                    @JsonProperty("generatedNino") String generatedNino,
                    @JsonProperty("generatedSurname") String generatedSurname,
                    @JsonProperty("generatedEmail") String generatedEmail,
                    @JsonProperty("generatedMobile") String generatedMobile,
                    @JsonProperty("generatedDOB") String generatedDob,
                    @JsonProperty("subscriptions") Subscriptions subscriptions,
                    @JsonProperty("regionalProcessingCenter")  RegionalProcessingCenter regionalProcessingCenter,
                    @JsonProperty("region") String region,
                    @JsonProperty(value = "ccdCaseId", access = JsonProperty.Access.WRITE_ONLY) String ccdCaseId) {
        this.caseReference = caseReference;
        this.appeal = appeal;
        this.hearings = hearings;
        this.evidence = evidence;
        this.dwpTimeExtension = dwpTimeExtension;
        this.events = events;
        this.generatedNino = generatedNino;
        this.generatedSurname = generatedSurname;
        this.generatedEmail = generatedEmail;
        this.generatedMobile = generatedMobile;
        this.generatedDob = generatedDob;
        this.subscriptions = subscriptions;
        this.regionalProcessingCenter = regionalProcessingCenter;
        this.region = region;
        this.ccdCaseId = ccdCaseId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CaseData caseData = (CaseData) o;
        return Objects.equals(caseReference, caseData.caseReference)
            && Objects.equals(appeal, caseData.appeal)
            && Objects.equals(hearings, caseData.hearings)
            && Objects.equals(evidence, caseData.evidence)
            && Objects.equals(dwpTimeExtension, caseData.dwpTimeExtension)
            && Objects.equals(events, caseData.events)
            && Objects.equals(generatedNino, caseData.generatedNino)
            && Objects.equals(generatedSurname, caseData.generatedSurname)
            && Objects.equals(generatedEmail, caseData.generatedEmail)
            && Objects.equals(generatedMobile, caseData.generatedMobile)
            && Objects.equals(generatedDob, caseData.generatedDob)
            && Objects.equals(subscriptions, caseData.subscriptions)
            && Objects.equals(regionalProcessingCenter, caseData.regionalProcessingCenter)
            && Objects.equals(region, caseData.region)
            && Objects.equals(ccdCaseId, caseData.ccdCaseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(caseReference, appeal, hearings, evidence, dwpTimeExtension, events, generatedNino,
            generatedSurname, generatedEmail, generatedMobile, generatedDob, subscriptions, regionalProcessingCenter,
            region, ccdCaseId);
    }
}
