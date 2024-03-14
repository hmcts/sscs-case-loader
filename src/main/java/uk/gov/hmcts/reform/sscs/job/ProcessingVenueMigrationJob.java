package uk.gov.hmcts.reform.sscs.job;

import static java.time.LocalDateTime.now;
import static uk.gov.hmcts.reform.sscs.ccd.domain.State.DORMANT_APPEAL_STATE;
import static uk.gov.hmcts.reform.sscs.ccd.domain.State.VOID_STATE;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.services.DataMigrationService;
import uk.gov.hmcts.reform.sscs.util.CaseLoaderTimerTask;

@Component
@Slf4j
public class ProcessingVenueMigrationJob extends DataMigrationJob {


    @Value("${features.venue-migration.enabled}")
    private boolean venueDataMigrationEnabled;

    @Value("${features.venue-migration.encoded-data-string}")
    private String venueEncodedDataString;

    @Value("${features.venue-migration.startHour}")
    private int migrationStartHour;

    @Value("${features.venue-migration.endHour}")
    private int migrationEndHour;

    @Value("${features.venue-migration.rollback}")
    public boolean isVenueRollback;


    public ProcessingVenueMigrationJob(CaseLoaderTimerTask caseLoaderTimerTask,
                                       DataMigrationService migrationService) {
        super(caseLoaderTimerTask, migrationService);
    }

    @Override
    public boolean readyToRun() {
        return venueDataMigrationEnabled
            && now().getHour() >= migrationStartHour
            && now().getHour() <= migrationEndHour;
    }

    @Override
    String getEncodedDataString() {
        return venueEncodedDataString;
    }

    @Override
    boolean isRollback() {
        return isVenueRollback;
    }

    public void updateCaseData(SscsCaseData caseData, String venue) {
        caseData.setProcessingVenue(venue);
        log.info("Setting processing venue value to ({})", caseData.getProcessingVenue());

    }

    public boolean shouldBeSkipped(SscsCaseDetails caseDetails, String venue) {
        var isInExcludedState = caseDetails.getState().equals(VOID_STATE.toString())
            || caseDetails.getState().equals(DORMANT_APPEAL_STATE.toString());
        boolean shouldBeSkipped = venue.equals(caseDetails.getData().getProcessingVenue())
            || isInExcludedState;
        if (shouldBeSkipped) {
            log.info(
                "Skipping case ({}) because venue already set ({})  or state={}",
                caseDetails.getId(), venue,
                caseDetails.getState()
            );
        }
        return shouldBeSkipped;
    }

}
