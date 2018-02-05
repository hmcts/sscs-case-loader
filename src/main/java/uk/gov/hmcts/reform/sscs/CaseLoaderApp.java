package uk.gov.hmcts.reform.sscs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import uk.gov.hmcts.reform.sscs.services.CaseLoaderService;

@SpringBootApplication
@EnableCircuitBreaker
@EnableHystrixDashboard
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
@EnableFeignClients
public class CaseLoaderApp implements CommandLineRunner {

    @Autowired
    private CaseLoaderService caseLoaderService;

    public static void main(String[] args) {
        SpringApplication.run(CaseLoaderApp.class, args);
    }

    @Override
    public void run(String... args) {
        try {
            caseLoaderService.process();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
