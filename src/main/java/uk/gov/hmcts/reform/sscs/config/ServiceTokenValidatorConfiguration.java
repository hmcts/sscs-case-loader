package uk.gov.hmcts.reform.sscs.config;

import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;
import uk.gov.hmcts.reform.authorisation.validators.ServiceAuthTokenValidator;

@Configuration
@Lazy
@EnableFeignClients(basePackageClasses = ServiceAuthorisationApi.class)
public class ServiceTokenValidatorConfiguration {

    @Bean
    public AuthTokenValidator authTokenValidator(
        ServiceAuthorisationApi serviceAuthorisationApi
    ) {
        return new ServiceAuthTokenValidator(serviceAuthorisationApi);
    }

}
