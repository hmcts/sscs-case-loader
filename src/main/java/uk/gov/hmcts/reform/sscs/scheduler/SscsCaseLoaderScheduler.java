package uk.gov.hmcts.reform.sscs.scheduler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.services.CaseLoaderService;

@Component
@Slf4j
@ConditionalOnProperty(value = "scheduling.enabled", havingValue = "true", matchIfMissing = true)
class SscsCaseLoaderScheduler {

    private final CaseLoaderService caseLoaderService;

    @Value("${slot.name}")
    private String slotName;

    @Autowired
    SscsCaseLoaderScheduler(CaseLoaderService caseLoaderService) {
        this.caseLoaderService = caseLoaderService;
    }

    @Scheduled(cron = "${sscs.case.loader.cron.schedule}")
    void run() {
        String logPrefix = "CASELOADER " + UUID.randomUUID().toString();

        log.info(logPrefix + " to process data using slot: {}", slotName);
        if ("PRODUCTION".equals(slotName)) {
            try {
                log.info(logPrefix + " scheduler started : {} ", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
                caseLoaderService.setLogPrefix(logPrefix);
                caseLoaderService.process();
                log.info(logPrefix + " scheduler ended : {} ", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            } catch (Exception e) {
                log.error(logPrefix + " scheduler failed at " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) 
                            + " due to exception: ", e);
            }
        }
    }

}
