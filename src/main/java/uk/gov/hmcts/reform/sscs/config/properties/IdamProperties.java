package uk.gov.hmcts.reform.sscs.config.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@Validated
@ConfigurationProperties(prefix = "idam")
@Getter
@Setter
//FIXME: this is no longer used and we should remove it together with its integraiton test
// However we CANNOT do it because it is causing the sscsCaseLoaderJob.run() to be run when the Spring context is loaded
public class IdamProperties {

    private Oauth2 oauth2;

    @Getter
    @Setter
    @ToString
    public static class Oauth2 {
        private User user;
        private Client client;
        private String redirectUrl;

        @Getter
        @Setter
        @ToString
        public static class User {
            private String email;
            private String password;
        }

        @Getter
        @Setter
        @ToString
        public static class Client {
            private String id;
            private String secret;
        }
    }
}
