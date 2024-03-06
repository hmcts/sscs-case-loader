package uk.gov.hmcts.reform.sscs.job;

import static java.time.LocalDateTime.now;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.sscs.job.ProcessingVenueMigrationJob.EXISTING_VENUE_COLUMN;
import static uk.gov.hmcts.reform.sscs.job.ProcessingVenueMigrationJob.MAPPED_VENUE_COLUMN;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.sscs.services.ProcessingVenueMigrationService;
import uk.gov.hmcts.reform.sscs.util.CaseLoaderTimerTask;

@ExtendWith(MockitoExtension.class)
class ProcessingVenueMigrationJobTest {

    @Mock
    CaseLoaderTimerTask timerTask;
    @Mock
    ProcessingVenueMigrationService migrationService;

    @Mock
    private Appender<ILoggingEvent> mockedAppender;

    @Captor
    private ArgumentCaptor<LoggingEvent> logEventCaptor;

    ProcessingVenueMigrationJob underTest;

    @BeforeEach
    void setUp() {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.addAppender(mockedAppender);
        root.setLevel(Level.INFO);
        underTest = new ProcessingVenueMigrationJob(timerTask, migrationService);
    }

    @ParameterizedTest
    @MethodSource("getStartHourScenarios")
    void shouldBeReadyToRunOnOrAfterStartTime(boolean migrationEnabled, int caseLoaderStartHour, boolean assertion) {
        ReflectionTestUtils.setField(underTest, "migrationStartHour", caseLoaderStartHour);
        ReflectionTestUtils.setField(underTest, "venueDataMigrationEnabled", migrationEnabled);

        assertEquals(underTest.readyToRun(), assertion);
    }

    @Test
    void shouldRunTheJob() {
        ReflectionTestUtils.setField(underTest, "isRollback", false);
        underTest.run();

        verify(mockedAppender, times(4)).doAppend(logEventCaptor.capture());
        var capturedLogs = logEventCaptor.getAllValues();
        assertEquals("{} scheduler started : {}", capturedLogs.get(0).getMessage());
        assertEquals("Processing Venue data migration job", capturedLogs.get(1).getFormattedMessage());
        assertEquals("{} scheduler ended : {}", capturedLogs.get(2).getMessage());
        assertEquals("Case loader Shutting down...", capturedLogs.get(3).getFormattedMessage());
    }

    @ParameterizedTest
    @MethodSource("getRollbackScenarios")
    void shouldProcessTheJob(boolean isRollback, String languageColumn) throws IOException {
        ReflectionTestUtils.setField(underTest, "isRollback", isRollback);
        underTest.process();

        verify(migrationService).process(languageColumn);
        verify(mockedAppender, atMostOnce()).doAppend(logEventCaptor.capture());
        String job = isRollback ? "rollback" : "migration";
        assertEquals(
            "Processing Venue data " + job + " job", logEventCaptor.getValue().getFormattedMessage()
        );
    }

    @Test
    void shouldProcessTheJob() throws IOException {
        doThrow(new IOException("Simulating decode failure")).when(migrationService).process(anyString());

        Exception exception = assertThrows(RuntimeException.class, () -> underTest.process());

        assertTrue(exception.getMessage().contains("Simulating decode failure"));
    }

    private static List<Arguments> getStartHourScenarios() {
        return List.of(
            Arguments.of(false, now().getHour(), false),
            Arguments.of(false, now().getHour() - 1, false),
            Arguments.of(false, now().getHour() + 1, false),
            Arguments.of(true, now().getHour(), true),
            Arguments.of(true, now().getHour() - 1, true),
            Arguments.of(true, now().getHour() + 1, false)
        );
    }

    private static List<Arguments> getRollbackScenarios() {
        return List.of(
            Arguments.of(true, EXISTING_VENUE_COLUMN),
            Arguments.of(false, MAPPED_VENUE_COLUMN)
        );
    }
}
