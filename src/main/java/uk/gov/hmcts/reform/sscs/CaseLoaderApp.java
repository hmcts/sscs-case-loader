package uk.gov.hmcts.reform.sscs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import uk.gov.hmcts.reform.sscs.models.JsonFiles;
import uk.gov.hmcts.reform.sscs.models.XmlFiles;
import uk.gov.hmcts.reform.sscs.services.CaseLoaderService;
import uk.gov.hmcts.reform.sscs.services.SftpCaseLoaderImpl;

import java.util.Optional;

@SpringBootApplication
@EnableCircuitBreaker
@EnableHystrixDashboard
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class CaseLoaderApp {

    public static void main(String[] args) {
        SpringApplication.run(CaseLoaderApp.class, args);
        start();
    }

    // TODO This is temporary only for testing purposes
    // Move and refactor into a better place once we know where that better place is
    private static void start() {
        CaseLoaderService caseLoaderService = new SftpCaseLoaderImpl();
        Optional<XmlFiles> optionalXmlFiles = caseLoaderService.fetchXmlFilesFromGaps2();
        if (optionalXmlFiles.isPresent()) {
            XmlFiles xmlFiles = optionalXmlFiles.get();
            boolean validateXmlFiles = caseLoaderService.validateXmlFiles(xmlFiles);
            if (validateXmlFiles) {
                JsonFiles jsonFiles = caseLoaderService.transformXmlFilesToJsonFiles(xmlFiles);
                System.out.println(jsonFiles);
            }
        }
    }
}
