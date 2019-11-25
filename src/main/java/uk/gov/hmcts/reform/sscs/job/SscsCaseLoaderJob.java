package uk.gov.hmcts.reform.sscs.job;

import com.microsoft.applicationinsights.TelemetryClient;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.services.CaseLoaderService;

@Component
@Slf4j
public class SscsCaseLoaderJob {

    private final CaseLoaderService caseLoaderService;
    private TelemetryClient telemetry = new TelemetryClient();

    @Autowired
    public SscsCaseLoaderJob(CaseLoaderService caseLoaderService) {
        this.caseLoaderService = caseLoaderService;
    }

    public void run() {
        String logPrefix = "CASELOADER " + UUID.randomUUID().toString();

        try {
            log.info(logPrefix + " scheduler started : {} ",
                        LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            caseLoaderService.setLogPrefix(logPrefix);
            caseLoaderService.process();
            log.info(logPrefix + " scheduler ended : {} ",
                        LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        } catch (Exception e) {
            log.error(logPrefix + " scheduler failed at "
                        + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
                        + " due to exception: ", e);
        } finally {
            telemetry.flush();
            //Allow some time for flushing before shutting down
            Thread.sleep(5000);
        }
    }

}
