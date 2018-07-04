package uk.gov.hmcts.reform.sscs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@SuppressWarnings("HideUtilityClassConstructor")
@EnableFeignClients
@Slf4j
@EnableScheduling
@EnableRetry
public class CaseLoaderApp {

    public static void main(String[] args) {
        SpringApplication.run(CaseLoaderApp.class, args);

    }
}
