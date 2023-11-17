package uk.gov.hmcts.reform.sscs.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.util.CaseLoaderTimerTask;

import static java.time.LocalDateTime.now;

@Component
@Slf4j
public class DataMigrationJob extends SscsJob {


    @Value("${sscs.case.loader.shutdown.delay.time}")
    private int shutdownDelayTime;

    @Value("${features.data-migration.interpreter}")
    private boolean interpreterDataMigrationEnabled;

    @Value("${sscs.case.loader.startTime}")
    private int caseLoaderStartTime;

    public DataMigrationJob(CaseLoaderTimerTask caseLoaderTimerTask) {
        super(caseLoaderTimerTask);
    }

    @Override
    public boolean readyToRun() {
        return interpreterDataMigrationEnabled && now().getHour() < caseLoaderStartTime;
    }

    public void process() {
        log.info("Processing Interpreter data migration job");
    }

}
