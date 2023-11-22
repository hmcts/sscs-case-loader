package uk.gov.hmcts.reform.sscs.job;

import static java.time.LocalDateTime.now;

import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.util.CaseLoaderTimerTask;

@Component
@Slf4j
public abstract class SscsJob {

    @Value("${sscs.case.loader.shutdown.delay.time}")
    private int shutdownDelayTime;

    private final CaseLoaderTimerTask caseLoaderTimerTask;

    @Autowired
    public SscsJob(CaseLoaderTimerTask caseLoaderTimerTask) {
        this.caseLoaderTimerTask = caseLoaderTimerTask;
    }

    abstract boolean readyToRun();

    abstract void process();

    public void run() {
        String logPrefix = "SSCS Job " + UUID.randomUUID().toString();

        try {
            log.info("{} scheduler started : {} ", logPrefix, now().format(DateTimeFormatter.ISO_DATE_TIME));
            process();
            log.info("{} scheduler ended : {} ", logPrefix, now().format(DateTimeFormatter.ISO_DATE_TIME));
        } catch (Exception e) {
            log.error("{} scheduler failed at {} due to exception: {}",
                logPrefix, now().format(DateTimeFormatter.ISO_DATE_TIME), e);
        }

        log.info("Case loader Shutting down...");
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.schedule(caseLoaderTimerTask, shutdownDelayTime, TimeUnit.MINUTES);
        executorService.shutdown();
    }
}
