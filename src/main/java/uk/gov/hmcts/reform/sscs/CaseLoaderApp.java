package uk.gov.hmcts.reform.sscs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;
import uk.gov.hmcts.reform.sscs.ccd.config.CcdRequestDetails;

@EnableBatchProcessing
@SpringBootApplication
@SuppressWarnings("HideUtilityClassConstructor")
@EnableFeignClients(basePackages = {
    "uk.gov.hmcts.reform.idam",
    "uk.gov.hmcts.reform.sscs.client"
})
@Slf4j
@EnableRetry
public class CaseLoaderApp {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(CaseLoaderApp.class, args);
        SpringApplication.exit(context);
    }

    @Bean
    public CcdRequestDetails getRequestDetails(
        @Value("${core_case_data.jurisdictionId}") String coreCaseDataJurisdictionId,
        @Value("${core_case_data.caseTypeId}") String coreCaseDataCaseTypeId) {
        return CcdRequestDetails.builder()
            .caseTypeId(coreCaseDataCaseTypeId)
            .jurisdictionId(coreCaseDataJurisdictionId)
            .build();
    }
}
