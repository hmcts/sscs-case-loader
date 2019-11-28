package uk.gov.hmcts.reform.sscs.job;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.services.CaseLoaderService;
import uk.gov.hmcts.reform.sscs.util.CaseLoaderTimerTask;

@Component
@Slf4j
public class SscsCaseLoaderJob {

    private final CaseLoaderService caseLoaderService;
    private static final int SHUTDOWN_DELAY_TIME = 7;

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
        }

        log.info("case loader Shutting down...");
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.schedule(new CaseLoaderTimerTask(), SHUTDOWN_DELAY_TIME, TimeUnit.MINUTES);
        executorService.shutdown();
    }

}
