package uk.gov.hmcts.reform.sscs.job;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.services.DataMigrationService;
import uk.gov.hmcts.reform.sscs.util.CaseLoaderTimerTask;

@Component
@Slf4j
public abstract class DataMigrationJob extends SscsJob {

    public static final String MAPPED_DATA_COLUMN = "mapped_data_value";
    public static final String EXISTING_DATA_COLUMN = "existing_data_value";

    private final DataMigrationService migrationService;



    public DataMigrationJob(CaseLoaderTimerTask caseLoaderTimerTask, DataMigrationService migrationService) {
        super(caseLoaderTimerTask);
        this.migrationService = migrationService;
    }

    @Override
    abstract boolean readyToRun();

    abstract String getEncodedDataString();

    public void process() {
        String migrationColumn = getIsRollback() ? EXISTING_DATA_COLUMN : MAPPED_DATA_COLUMN;
        log.info("Processing {} job", getIsRollback() ? "rollback" : "migration");
        try {
            migrationService.process(migrationColumn, this, getEncodedDataString());
        } catch (IOException e) {
            log.error("{} job failed to decode encodedDataString", getIsRollback() ? "rollback" : "migration", e);
            throw new RuntimeException(e);
        }
    }

    public abstract boolean shouldBeSkipped(SscsCaseDetails caseDetails, String fieldValue);

    abstract boolean getIsRollback();

    public abstract void updateCaseData(SscsCaseData caseData, String fieldValue);

}
