package uk.gov.hmcts.reform.sscs.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@Validated
@ConfigurationProperties(prefix = "idam")
@Getter
@Setter
public class IdamProperties {

    private Role role;

    @Setter
    @Getter
    public static class Role {
        private String email;
        private String password;

    }
}
