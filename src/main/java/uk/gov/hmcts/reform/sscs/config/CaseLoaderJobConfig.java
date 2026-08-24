package uk.gov.hmcts.reform.sscs.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.hmcts.reform.sscs.job.CaseLoaderTasklet;
import uk.gov.hmcts.reform.sscs.job.InterpreterMigrationJob;
import uk.gov.hmcts.reform.sscs.job.ProcessingVenueMigrationJob;
import uk.gov.hmcts.reform.sscs.job.SscsCaseLoaderJob;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class CaseLoaderJobConfig {

    private final SscsCaseLoaderJob sscsCaseLoaderJob;
    private final ProcessingVenueMigrationJob venueMigrationJob;
    private final InterpreterMigrationJob interpreterMigrationJob;


    @Bean
    public Job caseLoaderJob(
        JobRepository jobRepository,
        Step caseLoaderStep) {
        return new JobBuilder(
            "caseLoaderJob",
            jobRepository)
            .start(caseLoaderStep)
            .build();
    }

    @Bean
    public Step caseLoaderStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager) {

        return new StepBuilder("caseLoaderStep", jobRepository)
            .tasklet(tasklet(), transactionManager)
            .build();
    }

    public Tasklet tasklet() {
        return new CaseLoaderTasklet(sscsCaseLoaderJob, interpreterMigrationJob, venueMigrationJob);
    }
}


