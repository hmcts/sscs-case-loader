package uk.gov.hmcts.reform.sscs;

import com.jcraft.jsch.JSch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.context.annotation.Bean;
import org.xml.sax.SAXException;
import uk.gov.hmcts.reform.sscs.models.JsonFiles;
import uk.gov.hmcts.reform.sscs.models.XmlFiles;
import uk.gov.hmcts.reform.sscs.services.mapper.TransformJsonCasesToCaseData;
import uk.gov.hmcts.reform.sscs.services.mapper.TransformXmlFilesToJsonFiles;
import uk.gov.hmcts.reform.sscs.services.xml.FetchXmlFilesService;
import uk.gov.hmcts.reform.sscs.services.xml.XmlValidator;

import java.io.IOException;
import java.util.Optional;
import javax.xml.stream.XMLStreamException;

@SpringBootApplication
@EnableCircuitBreaker
@EnableHystrixDashboard
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class CaseLoaderApp {

    @Autowired
    private FetchXmlFilesService sftpFetchXmlFilesService;
    @Autowired
    private TransformXmlFilesToJsonFiles transformXmlFilesToJsonFiles;
    @Autowired
    private TransformJsonCasesToCaseData transformJsonCasesToCaseData;
    @Autowired
    private XmlValidator xmlValidator;

    @Bean
    public JSch jschSshChannel() {
        return new JSch();
    }

    public static void main(String[] args) {
        SpringApplication.run(CaseLoaderApp.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            Optional<XmlFiles> optionalXmlFiles = sftpFetchXmlFilesService.fetch();
            if (optionalXmlFiles.isPresent()) {
                XmlFiles xmlFiles = optionalXmlFiles.get();
                validateXmls(xmlFiles);
                JsonFiles jsonFiles = transformXmlFilesToJsonFiles.transform(xmlFiles);
                transformJsonCasesToCaseData.transform(jsonFiles.getDelta().toString());
            }
        };
    }

    private void validateXmls(XmlFiles xmlFiles) throws IOException, SAXException, XMLStreamException {
        xmlValidator.validateXml(xmlFiles.getDelta());
        xmlValidator.validateXml(xmlFiles.getRef());
    }

}
