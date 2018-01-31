package uk.gov.hmcts.reform.sscs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.sscs.services.xml.FetchXmlFilesService;
import uk.gov.hmcts.reform.sscs.services.xml.SftpFetchXmlFilesImpl;

@Configuration
public class FetchXmlFilesServiceConfiguration {

    @Bean
    public FetchXmlFilesService sftpFetchXmlFilesService() {
        return new SftpFetchXmlFilesImpl();
    }

}

