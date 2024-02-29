package uk.gov.hmcts.reform.sscs.job;

import static java.time.LocalDateTime.now;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.services.DataMigrationService;
import uk.gov.hmcts.reform.sscs.util.CaseLoaderTimerTask;

@Component
@Slf4j
public class DataMigrationJob extends SscsJob {

    public static final String MAPPED_LANGUAGE_COLUMN = "mapped_language_value";
    public static final String EXISTING_LANGUAGE_COLUMN = "existing_language_value";

    private DataMigrationService migrationService;

    @Value("${features.data-migration.interpreter}")
    private boolean interpreterDataMigrationEnabled;

    @Value("${features.data-migration.rollback}")
    private boolean isRollback;

    @Value("${sscs.case.loader.startHour}")
    private int caseLoaderStartHour;

    public DataMigrationJob(CaseLoaderTimerTask caseLoaderTimerTask, DataMigrationService migrationService) {
        super(caseLoaderTimerTask);
        this.migrationService = migrationService;
    }

    @Override
    public boolean readyToRun() {
        return interpreterDataMigrationEnabled && now().getHour() >= caseLoaderStartHour;
    }

    public void process() {
        String languageColumn = isRollback ? EXISTING_LANGUAGE_COLUMN : MAPPED_LANGUAGE_COLUMN;
        log.info("Processing Interpreter data {} job", isRollback ? "rollback" : "migration");
        try {
            migrationService.process(languageColumn);
        } catch (IOException e) {
            log.error("{} job failed to decode encodedDataString", isRollback ? "rollback" : "migration", e);
            throw new RuntimeException(e);
        }
    }

}
