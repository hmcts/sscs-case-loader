package uk.gov.hmcts.reform.sscs.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.sscs.job.DataMigrationJob.MAPPED_DATA_COLUMN;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.job.DataMigrationJob;
import uk.gov.hmcts.reform.sscs.services.ccd.CcdCasesSender;


@ExtendWith(MockitoExtension.class)
class DataMigrationServiceTest {

    private static final String COMPRESSSED_ENCODED_DATA_STRING = "eJyLrlYqSk1LLUrNS05VslIyNLO0NDYxNzUzsTQztTAw"
        + "VtJRSq3ILC7JzEuPT0ksSYwvS8wpBSkMLkjMyyzOAMrnJhYUpKagyjoWJSZlJivV6qCZbm5gZmFgZm5kZmJsaWxugNN0N5AOXIY7pe"
        + "alJ+ZkKtXGAgB1Yj3B";

    @Mock
    private CcdCasesSender ccdCasesSender;
    @Mock
    private IdamService idamService;
    @Mock
    private Appender<ILoggingEvent> mockedAppender;

    @Mock
    private DataMigrationJob job;

    @Captor
    private ArgumentCaptor<LoggingEvent> logEventCaptor;

    DataMigrationService underTest;

    @BeforeEach
    void setUp() {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.addAppender(mockedAppender);
        root.setLevel(Level.INFO);
        underTest = new DataMigrationService(ccdCasesSender, idamService);
    }

    @Test
    void shouldProcessCases() throws IOException {
        IdamTokens tokens = IdamTokens.builder().build();
        when(idamService.getIdamTokens()).thenReturn(tokens);
        when(ccdCasesSender.updateCaseMigration(eq(1699347564965803L), eq(tokens), eq("Arabic"), eq(job)))
            .thenReturn(true);
        when(ccdCasesSender.updateCaseMigration(eq(1706806726439370L), eq(tokens), eq("Bengali"), eq(job)))
            .thenReturn(false);

        underTest.process(MAPPED_DATA_COLUMN, job, COMPRESSSED_ENCODED_DATA_STRING);

        verify(mockedAppender, times(2)).doAppend(logEventCaptor.capture());
        var capturedLogs = logEventCaptor.getAllValues();
        assertEquals("Number of cases to be migrated: (2)", capturedLogs.get(0).getFormattedMessage());
        assertEquals("Number of unprocessed cases: (1)", capturedLogs.get(1).getFormattedMessage());
    }
}
