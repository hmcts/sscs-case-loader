package uk.gov.hmcts.reform.sscs.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
public class CaseLoaderTasklet implements Tasklet {

    private SscsCaseLoaderJob sscsCaseLoaderJob;
    private InterpreterMigrationJob interpreterMigrationJob;
    private ProcessingVenueMigrationJob venueMigrationJob;

    public CaseLoaderTasklet(SscsCaseLoaderJob sscsCaseLoaderJob,
                             InterpreterMigrationJob interpreterMigrationJob,
                             ProcessingVenueMigrationJob venueMigrationJob) {
        this.sscsCaseLoaderJob = sscsCaseLoaderJob;
        this.interpreterMigrationJob = interpreterMigrationJob;
        this.venueMigrationJob = venueMigrationJob;
    }


    @Override
    public RepeatStatus execute(StepContribution stepContribution,
                                ChunkContext chunkContext) {

        runJob(sscsCaseLoaderJob);
        runJob(interpreterMigrationJob);
        runJob(venueMigrationJob);

        return RepeatStatus.FINISHED;
    }

    private void runJob(SscsJob job) {
        if (job.readyToRun()) {
            log.info("============== About to run {} job ==============", job.getClass().getSimpleName());
            job.run();
            log.info("============== {} Job complete ==============", job.getClass().getSimpleName());
        }
    }
}
