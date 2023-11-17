package uk.gov.hmcts.reform.sscs.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.time.LocalDateTime;

@Slf4j
public class CaseLoaderTasklet implements Tasklet {

    private SscsCaseLoaderJob sscsCaseLoaderJob;
    private DataMigrationJob dataMigrationJob;

    private static final int CASE_LOADER_START_TIME = 10;

    public CaseLoaderTasklet(SscsCaseLoaderJob sscsCaseLoaderJob, DataMigrationJob dataMigrationJob) {
        this.sscsCaseLoaderJob = sscsCaseLoaderJob;
        this.dataMigrationJob = dataMigrationJob;
    }


    @Override
    public RepeatStatus execute(StepContribution stepContribution,
                                ChunkContext chunkContext) {

        LocalDateTime now = java.time.LocalDateTime.now();

        log.info("============== About to run job ==============");

        if (now.getHour() >= CASE_LOADER_START_TIME) {
            sscsCaseLoaderJob.run();
        } else {
            dataMigrationJob.run();
        }

        log.info("============== Job complete ==============");

        return RepeatStatus.FINISHED;
    }
}
