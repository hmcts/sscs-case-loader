package uk.gov.hmcts.reform.sscs.config.properties;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@Validated
@ConfigurationProperties(prefix = "ccd")
@Getter
@Setter
public class CoreCaseDataProperties {
    @NonNull
    private String jurisdictionId;
    @NonNull
    private String caseTypeId;
}
