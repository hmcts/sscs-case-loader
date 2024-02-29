package uk.gov.hmcts.reform.sscs.job;

import static java.time.LocalDateTime.now;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.services.ProcessingVenueMigrationService;
import uk.gov.hmcts.reform.sscs.util.CaseLoaderTimerTask;

@Component
@Slf4j
public class ProcessingVenueMigrationJob extends SscsJob {

    public static final String MAPPED_VENUE_COLUMN = "mapped_venue_value";
    public static final String EXISTING_VENUE_COLUMN = "existing_venue_value";

    private ProcessingVenueMigrationService migrationService;

    @Value("${features.venue-migration.enabled}")
    private boolean venueDataMigrationEnabled;

    @Value("${features.venue-migration.rollback}")
    private boolean isRollback;

    @Value("${sscs.case.loader.startHour}")
    private int caseLoaderStartHour;

    public ProcessingVenueMigrationJob(CaseLoaderTimerTask caseLoaderTimerTask,
                                       ProcessingVenueMigrationService migrationService) {
        super(caseLoaderTimerTask);
        this.migrationService = migrationService;
    }

    @Override
    public boolean readyToRun() {
        return venueDataMigrationEnabled && now().getHour() >= caseLoaderStartHour;
    }

    public void process() {
        String venueColumn = isRollback ? EXISTING_VENUE_COLUMN : MAPPED_VENUE_COLUMN;
        log.info("Processing Venue data {} job", isRollback ? "rollback" : "migration");
        try {
            migrationService.process(venueColumn);
        } catch (IOException e) {
            log.error("{} job failed to decode encodedDataString", isRollback ? "rollback" : "migration", e);
            throw new RuntimeException(e);
        }
    }

}
