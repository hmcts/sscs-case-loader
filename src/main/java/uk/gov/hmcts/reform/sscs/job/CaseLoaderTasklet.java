package uk.gov.hmcts.reform.sscs.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
public class CaseLoaderTasklet implements Tasklet {

    private SscsCaseLoaderJob sscsCaseLoaderJob;

    public CaseLoaderTasklet(SscsCaseLoaderJob sscsCaseLoaderJob) {
        this.sscsCaseLoaderJob = sscsCaseLoaderJob;
    }


    @Override
    public RepeatStatus execute(StepContribution stepContribution,
                                ChunkContext chunkContext) {

        log.info("About to run case loader job.");

        sscsCaseLoaderJob.run();

        log.info("Case loader job complete.");

        return RepeatStatus.FINISHED;
    }
}
