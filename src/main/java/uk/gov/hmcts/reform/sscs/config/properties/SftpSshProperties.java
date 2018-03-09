package uk.gov.hmcts.reform.sscs.config.properties;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import uk.gov.hmcts.reform.sscs.services.gaps2.files.Gaps2File;

@Configuration
@Validated
@ConfigurationProperties(prefix = "sftp")
public class SftpSshProperties {

    @NotBlank
    private String host;
    private int port;
    @NotBlank
    private String username;
    @NotBlank
    private String inputDirectory;
    @NotBlank
    private String keyLocation;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getInputDirectory() {
        return inputDirectory;
    }

    public void setInputDirectory(String inputDirectory) {
        this.inputDirectory = inputDirectory;
    }

    public String getKeyLocation() {
        return keyLocation;
    }

    public void setKeyLocation(String keyLocation) {
        this.keyLocation = keyLocation;
    }

    public String getProcessedFile(Gaps2File file) {
        return String.format("%s/%s/%s", getInputDirectory(), "processed", file.getName());
    }

    public String getFailedFile(Gaps2File file) {
        return String.format("%s/%s/%s", getInputDirectory(), "failed", file.getName());
    }
}
