package uk.gov.hmcts.reform.sscs.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
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

    @Bean
    public Job caseLoaderJob() {
        return jobBuilders.get("caseLoaderJob")
            .start(taskletStep())
            .build();
    }

    @Bean
    public Step taskletStep() {
        return stepBuilders.get("taskletStep")
            .tasklet(tasklet())
            .build();
    }

    @Lazy
    @Bean
    public Tasklet tasklet() {

        log.info("About to run case loader job.");

        sscsCaseLoaderJob.run();

        log.info("Case loader job complete.");

        return (contribution, chunkContext) -> RepeatStatus.FINISHED;
    }
}


