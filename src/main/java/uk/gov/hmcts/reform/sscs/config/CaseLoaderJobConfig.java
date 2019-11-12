package uk.gov.hmcts.reform.sscs.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.job.SscsCaseLoaderJob;

@Service
@Slf4j
public class CaseLoaderJobConfig {

    @Autowired
    private JobBuilderFactory jobBuilders;

    @Autowired
    private StepBuilderFactory stepBuilders;

    @Autowired
    private SscsCaseLoaderJob sscsCaseLoaderJob;

    @Autowired
    private JobLauncher jobLauncher;


    @EventListener
    public Job caseLoaderJob(ContextRefreshedEvent event) throws
        JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException,
        JobInstanceAlreadyCompleteException {

        Job caseLoaderJob = jobBuilders.get("caseLoaderJob")
            .start(taskletStep())
            .build();
        jobLauncher.run(caseLoaderJob, new JobParameters());
        return caseLoaderJob;
    }

    private Step taskletStep() {
        return stepBuilders.get("taskletStep")
            .tasklet((contribution, chunkContext) -> {
                sscsCaseLoaderJob.run();
                return RepeatStatus.FINISHED;
            })
            .build();
    }

}


