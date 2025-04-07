package uk.gov.hmcts.reform.sscs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.sscs.job.CaseLoaderTasklet;
import uk.gov.hmcts.reform.sscs.job.InterpreterMigrationJob;
import uk.gov.hmcts.reform.sscs.job.ProcessingVenueMigrationJob;
import uk.gov.hmcts.reform.sscs.job.SscsCaseLoaderJob;

@SpringBootTest
public class SmokeTest {

    @Autowired
    private SscsCaseLoaderJob caseLoaderJob;

    @Autowired
    private InterpreterMigrationJob interpreterMigrationJob;

    @Autowired
    private ProcessingVenueMigrationJob venueMigrationJob;

    CaseLoaderTasklet underTest;

    @BeforeEach
    public void setup() {
        underTest = new CaseLoaderTasklet(caseLoaderJob, interpreterMigrationJob, venueMigrationJob);
    }

    @Test
    public void shouldExecuteCaseLoaderJob() {
        underTest.execute(null, null);
    }
}
