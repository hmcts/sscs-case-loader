package uk.gov.hmcts.reform.sscs.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.sscs.job.CaseLoaderTasklet;
import uk.gov.hmcts.reform.sscs.job.InterpreterMigrationJob;
import uk.gov.hmcts.reform.sscs.job.ProcessingVenueMigrationJob;
import uk.gov.hmcts.reform.sscs.job.SscsCaseLoaderJob;

@Configuration
@Slf4j
public class CaseLoaderJobConfig {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private SscsCaseLoaderJob sscsCaseLoaderJob;
    @Autowired
    private ProcessingVenueMigrationJob venueMigrationJob;
    @Autowired
    private InterpreterMigrationJob interpreterMigrationJob;

    @Bean
    public Job caseLoaderJob(Step caseLoaderStep) {
        JobBuilder jobBuilders = new JobBuilder("caseLoaderJob");
        return jobBuilders.start(caseLoaderStep)
            .build();
    }

    @Bean
    public Step caseLoaderStep() {
        StepBuilder stepBuilders = new StepBuilder("caseLoaderStep", jobRepository);
        return stepBuilders.tasklet(tasklet())
            .build();
    }

    public Tasklet tasklet() {

        return new CaseLoaderTasklet(sscsCaseLoaderJob, interpreterMigrationJob, venueMigrationJob);

    }
}


