package uk.gov.hmcts.reform.sscs.job;

import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CaseLoaderTaskletTest {

    @Mock
    private SscsCaseLoaderJob caseLoaderJob;

    @Mock
    private InterpreterMigrationJob interpreterMigrationJob;

    @Mock
    private ProcessingVenueMigrationJob venueMigrationJob;

    CaseLoaderTasklet underTest;

    @BeforeEach
    public void setup() {
        underTest = new CaseLoaderTasklet(caseLoaderJob, interpreterMigrationJob, venueMigrationJob);
    }

    @Test
    public void shouldExecuteCaseLoaderJob() {
        when(caseLoaderJob.readyToRun()).thenReturn(true);
        when(interpreterMigrationJob.readyToRun()).thenReturn(false);
        when(venueMigrationJob.readyToRun()).thenReturn(false);

        underTest.execute(null, null);

        verify(caseLoaderJob, atMostOnce()).run();
        verify(interpreterMigrationJob, never()).run();
        verify(venueMigrationJob, never()).run();
    }

    @Test
    public void shouldExecuteDataMigrationJob() {
        when(caseLoaderJob.readyToRun()).thenReturn(false);
        when(interpreterMigrationJob.readyToRun()).thenReturn(true);
        when(venueMigrationJob.readyToRun()).thenReturn(true);

        underTest.execute(null, null);

        verify(caseLoaderJob, never()).run();
        verify(interpreterMigrationJob, atMostOnce()).run();
        verify(venueMigrationJob, atMostOnce()).run();
    }
}
