package uk.gov.hmcts.reform.sscs.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.util.CaseLoaderTimerTask;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class DataMigrationJob {


    @Value("${sscs.case.loader.shutdown.delay.time}")
    private int shutdownDelayTime;

    private final CaseLoaderTimerTask caseLoaderTimerTask;

    @Autowired
    public DataMigrationJob(CaseLoaderTimerTask caseLoaderTimerTask) {
        this.caseLoaderTimerTask = caseLoaderTimerTask;
    }

    public void run() {
        String logPrefix = "Interpreter language Data Migration " + UUID.randomUUID().toString();

        try {
            log.info(logPrefix + " scheduler started : {} ",
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            log.info(logPrefix + " scheduler ended : {} ",
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        } catch (Exception e) {
            log.error(logPrefix + " scheduler failed at "
                + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
                + " due to exception: ", e);
        }

        log.info("case loader Shutting down...");
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.schedule(caseLoaderTimerTask, shutdownDelayTime, TimeUnit.MINUTES);
        executorService.shutdown();
    }

}
