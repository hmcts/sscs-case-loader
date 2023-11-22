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
    private DataMigrationJob dataMigrationJob;

    CaseLoaderTasklet underTest;

    @BeforeEach
    public void setup() {
        underTest = new CaseLoaderTasklet(caseLoaderJob, dataMigrationJob);
    }

    @Test
    public void shouldExecuteCaseLoaderJob() {
        when(caseLoaderJob.readyToRun()).thenReturn(true);
        when(dataMigrationJob.readyToRun()).thenReturn(false);

        underTest.execute(null, null);

        verify(caseLoaderJob, atMostOnce()).run();
        verify(dataMigrationJob, never()).run();
    }

    @Test
    public void shouldExecuteDataMigrationJob() {
        when(caseLoaderJob.readyToRun()).thenReturn(false);
        when(dataMigrationJob.readyToRun()).thenReturn(true);

        underTest.execute(null, null);

        verify(caseLoaderJob, never()).run();
        verify(dataMigrationJob, atMostOnce()).run();
    }
}
