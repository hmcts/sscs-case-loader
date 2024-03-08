package uk.gov.hmcts.reform.sscs.job;

import static java.time.LocalDateTime.now;
import static uk.gov.hmcts.reform.sscs.ccd.domain.State.DORMANT_APPEAL_STATE;
import static uk.gov.hmcts.reform.sscs.ccd.domain.State.VOID_STATE;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.YesNo;
import uk.gov.hmcts.reform.sscs.services.DataMigrationService;
import uk.gov.hmcts.reform.sscs.util.CaseLoaderTimerTask;

@Component
@Slf4j
public class InterpreterMigrationJob extends DataMigrationJob {


    @Value("${features.data-migration.interpreter}")
    private boolean interpreterDataMigrationEnabled;


    @Value("${sscs.case.loader.startHour}")
    private int caseLoaderStartHour;

    @Value("${features.data-migration.encoded-data-string}")
    private String interpreterEncodedDataString;

    @Value("${features.data-migration.rollback}")
    public  boolean isInterpreterRollback;



    public InterpreterMigrationJob(CaseLoaderTimerTask caseLoaderTimerTask, DataMigrationService migrationService) {
        super(caseLoaderTimerTask, migrationService);
    }


    @Override
    public boolean readyToRun() {
        return interpreterDataMigrationEnabled && now().getHour() >= caseLoaderStartHour;
    }

    @Override
    String getEncodedDataString() {
        return interpreterEncodedDataString;
    }



    public void updateCaseData(SscsCaseData caseData, String language) {
        log.info("Language value  ({})", language);
        caseData.getAppeal().getHearingOptions().setLanguages(language);

    }

    public boolean shouldBeSkipped(SscsCaseDetails caseDetails, String language) {
        var isInExcludedState = caseDetails.getState().equals(VOID_STATE.toString())
            || caseDetails.getState().equals(DORMANT_APPEAL_STATE.toString());
        var needInterpreter = YesNo.YES.getValue()
            .equals(caseDetails.getData().getAppeal().getHearingOptions().getLanguageInterpreter());
        boolean shouldBeSkipped = language.equals(caseDetails.getData().getAppeal().getHearingOptions().getLanguages())
            || isInExcludedState
            || needInterpreter;
        if (shouldBeSkipped) {
            log.info(
                "Skipping case ({}) because language already set ({}) OR Interpreter={} or state={}",
                caseDetails.getId(),
                language,
                caseDetails.getData().getAppeal().getHearingOptions().getLanguageInterpreter(),
                caseDetails.getState()
            );
        }
        return shouldBeSkipped;
    }

    @Override
    boolean getIsRollback() {
        return isInterpreterRollback;
    }


}
