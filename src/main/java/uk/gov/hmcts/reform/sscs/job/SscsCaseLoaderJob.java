package uk.gov.hmcts.reform.sscs.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.services.CaseLoaderService;
import uk.gov.hmcts.reform.sscs.util.CaseLoaderTimerTask;

import java.util.UUID;

import static java.time.LocalDateTime.now;

@Component
@Slf4j
public class SscsCaseLoaderJob extends SscsJob {

    private final CaseLoaderService caseLoaderService;

    @Value("${sscs.case.loader.shutdown.delay.time}")
    private int shutdownDelayTime;

    @Value("${sscs.case.loader.startTime}")
    private int caseLoaderStartTime;

    @Autowired
    public SscsCaseLoaderJob(CaseLoaderService caseLoaderService, CaseLoaderTimerTask caseLoaderTimerTask) {
        super(caseLoaderTimerTask);
        this.caseLoaderService = caseLoaderService;
    }

    public boolean readyToRun() {
        return now().getHour() >= caseLoaderStartTime;
    }

    public void process() {
        String logPrefix = "CASELOADER " + UUID.randomUUID().toString();
        caseLoaderService.setLogPrefix(logPrefix);
        caseLoaderService.process();
    }

}
