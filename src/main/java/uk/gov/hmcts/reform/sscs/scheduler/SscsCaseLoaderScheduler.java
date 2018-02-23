package uk.gov.hmcts.reform.sscs.scheduler;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.services.CaseLoaderService;

@Component
@Slf4j
@ConditionalOnProperty(value = "scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class SscsCaseLoaderScheduler {

    private final CaseLoaderService caseLoaderService;

    @Autowired
    public SscsCaseLoaderScheduler(CaseLoaderService caseLoaderService) {
        this.caseLoaderService = caseLoaderService;
    }

    @Scheduled(cron = "${sscs.case.loader.cron.schedule}")
    public void run() {
        try {
            log.info("SSCS Case loader started : {} ", LocalDateTime.now());
            caseLoaderService.process();
            log.info("SSCS Case loader Ended : {} ", LocalDateTime.now());
        } catch (Exception e) {
            log.error("SSCS Case loader failed :", e);
        }
    }

}
