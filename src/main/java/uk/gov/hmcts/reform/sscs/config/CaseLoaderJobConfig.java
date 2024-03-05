package uk.gov.hmcts.reform.sscs.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.sscs.job.CaseLoaderTasklet;
import uk.gov.hmcts.reform.sscs.job.ProcessingVenueMigrationJob;
import uk.gov.hmcts.reform.sscs.job.SscsCaseLoaderJob;

@Configuration
@Slf4j
public class CaseLoaderJobConfig {

    @Autowired
    private JobBuilderFactory jobBuilders;

    @Autowired
    private StepBuilderFactory stepBuilders;

    @Autowired
    private SscsCaseLoaderJob sscsCaseLoaderJob;
    @Autowired
    private ProcessingVenueMigrationJob venueMigrationJob;

    @Bean
    public Job caseLoaderJob(Step caseLoaderStep) {
        return jobBuilders.get("caseLoaderJob").start(caseLoaderStep)
            .build();
    }

    @Bean
    public Step caseLoaderStep() {
        return stepBuilders.get("caseLoaderStep")
            .tasklet(tasklet())
            .build();
    }

    public Tasklet tasklet() {

        return new CaseLoaderTasklet(sscsCaseLoaderJob, venueMigrationJob);

    }
}


