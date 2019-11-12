package uk.gov.hmcts.reform.sscs.job;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.services.CaseLoaderService;

@Component
@Slf4j
@ConditionalOnProperty(value = "scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class SscsCaseLoaderJob {

    private final CaseLoaderService caseLoaderService;
    private ApplicationContext applicationContext;

    @Autowired
    public SscsCaseLoaderJob(CaseLoaderService caseLoaderService, ApplicationContext applicationContext) {
        this.caseLoaderService = caseLoaderService;
        this.applicationContext = applicationContext;
    }

    @Scheduled(cron = "${sscs.case.loader.cron.schedule}")
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
        int exitCode = SpringApplication.exit(applicationContext, () -> 0);
        System.exit(exitCode);
    }

}
