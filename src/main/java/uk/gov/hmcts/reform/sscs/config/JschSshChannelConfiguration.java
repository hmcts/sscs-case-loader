package uk.gov.hmcts.reform.sscs.config;

import com.jcraft.jsch.JSch;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JschSshChannelConfiguration {

    @Bean
    public JSch jschSshChannel() {
        return new JSch();
    }
}
