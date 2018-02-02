package uk.gov.hmcts.reform.sscs;

import com.jcraft.jsch.JSch;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.context.annotation.Bean;
import org.xml.sax.SAXException;
import uk.gov.hmcts.reform.sscs.services.service.CaseLoaderService;

@SpringBootApplication
@EnableCircuitBreaker
@EnableHystrixDashboard
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class CaseLoaderApp implements CommandLineRunner {

    @Autowired
    private CaseLoaderService caseLoaderService;

    @Bean
    public JSch jschSshChannel() {
        return new JSch();
    }

    public static void main(String[] args) {
        SpringApplication.run(CaseLoaderApp.class, args);
    }

    @Override
    public void run(String... args) throws IOException, SAXException, XMLStreamException {
        caseLoaderService.process();
    }


}
