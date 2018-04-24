package uk.gov.hmcts.reform.sscs.models.serialize.ccd;

import com.fasterxml.jackson.annotation.*;

import java.util.List;

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
    private Subscriptions subscriptions;
    private RegionalProcessingCenter regionalProcessingCenter;
    private String region;

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
                    @JsonProperty("subscriptions") Subscriptions subscriptions,
                    @JsonProperty("regionalProcessingCenter")  RegionalProcessingCenter regionalProcessingCenter,
                    @JsonProperty("region") String region) {
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
        this.subscriptions = subscriptions;
        this.regionalProcessingCenter = regionalProcessingCenter;
        this.region = region;
    }
}
