package uk.gov.hmcts.reform.sscs.scheduler;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.services.CaseLoaderService;

@Component
@Slf4j
public class SscsCaseLoaderScheduler {

    private CaseLoaderService caseLoaderService;

    @Autowired
    public SscsCaseLoaderScheduler(CaseLoaderService caseLoaderService) {
        this.caseLoaderService = caseLoaderService;
    }

    @Scheduled(cron = "${sscs.case.loader.cron.schedule}")
    public void run() {
        log.info("SSCS Case loader started : {} ", LocalDateTime.now());
        caseLoaderService.process();
        log.info("SSCS Case loader Ended : {} ", LocalDateTime.now());
    }

}
