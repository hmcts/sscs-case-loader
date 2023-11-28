package uk.gov.hmcts.reform.sscs.job;

import static java.time.LocalDateTime.now;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.services.CaseLoaderService;
import uk.gov.hmcts.reform.sscs.util.CaseLoaderTimerTask;

@Component
@Slf4j
public class SscsCaseLoaderJob extends SscsJob {

    private final CaseLoaderService caseLoaderService;

    @Value("${sscs.case.loader.endHour}")
    private int caseLoaderEndHour;

    @Autowired
    public SscsCaseLoaderJob(CaseLoaderService caseLoaderService, CaseLoaderTimerTask caseLoaderTimerTask) {
        super(caseLoaderTimerTask);
        this.caseLoaderService = caseLoaderService;
    }

    public boolean readyToRun() {
        return now().getHour() <= caseLoaderEndHour;
    }

    public void process() {
        caseLoaderService.setLogPrefix("CASELOADER " + UUID.randomUUID());
        caseLoaderService.process();
    }

}
