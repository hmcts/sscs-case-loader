package uk.gov.hmcts.reform.sscs.config.properties;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "core_case_data")
public class CoreCaseDataProperties {
    @NotBlank
    private String userId;
    @NotBlank
    private String jurisdictionId;
    @NotBlank
    private String caseTypeId;
    @NotBlank
    private String eventId;

    public String getUserId() {
        return userId;
    }

    public String getJurisdictionId() {
        return jurisdictionId;
    }

    public String getCaseTypeId() {
        return caseTypeId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setJurisdictionId(String jurisdictionId) {
        this.jurisdictionId = jurisdictionId;
    }

    public void setCaseTypeId(String caseTypeId) {
        this.caseTypeId = caseTypeId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
}
