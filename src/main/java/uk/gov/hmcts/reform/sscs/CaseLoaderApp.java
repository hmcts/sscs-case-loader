package uk.gov.hmcts.reform.sscs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.context.annotation.Bean;
import uk.gov.hmcts.reform.sscs.models.XmlFiles;
import uk.gov.hmcts.reform.sscs.services.FetchXmlFilesService;
import uk.gov.hmcts.reform.sscs.services.TransformXmlFilesToJsonFilesService;
import uk.gov.hmcts.reform.sscs.services.ValidateXmlFilesService;

import java.util.Optional;

@SpringBootApplication
@EnableCircuitBreaker
@EnableHystrixDashboard
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class CaseLoaderApp {

    @Autowired
    private FetchXmlFilesService sftpFetchXmlFilesService;
    @Autowired
    private TransformXmlFilesToJsonFilesService transformXmlFilesToJsonFilesService;
    @Autowired
    private ValidateXmlFilesService validateXmlFilesService;

    public static void main(String[] args) {
        SpringApplication.run(CaseLoaderApp.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            Optional<XmlFiles> optionalXmlFiles = sftpFetchXmlFilesService.fetch();
            if (optionalXmlFiles.isPresent()) {
                XmlFiles xmlFiles = optionalXmlFiles.get();
                boolean validateXmlFiles = validateXmlFilesService.validate(xmlFiles);
                if (validateXmlFiles) {
                    transformXmlFilesToJsonFilesService.transform(xmlFiles);
                }
            }
        };
    }

}
