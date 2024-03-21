package uk.gov.hmcts.reform.sscs.job;

import static java.time.LocalDateTime.now;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.sscs.job.ProcessingVenueMigrationJob.EXISTING_DATA_COLUMN;
import static uk.gov.hmcts.reform.sscs.job.ProcessingVenueMigrationJob.MAPPED_DATA_COLUMN;

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
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.services.DataMigrationService;
import uk.gov.hmcts.reform.sscs.util.CaseLoaderTimerTask;

@ExtendWith(MockitoExtension.class)
class ProcessingVenueMigrationJobTest {

    @Mock
    CaseLoaderTimerTask timerTask;
    @Mock
    DataMigrationService migrationService;

    @Mock
    private Appender<ILoggingEvent> mockedAppender;

    @Captor
    private ArgumentCaptor<LoggingEvent> logEventCaptor;

    private static final String COMPRESSSED_ENCODED_DATA_STRING = "eJzNUT1PwzAQ/SuW5w52mjqGrXTtCANCKLraR7AUO5F9qahQ/z"
        + "sXgRBf7dIFDyfZfvfuvXcPrzLjE2ZMDuW11I1aqkpfVbVRqtZWLiS/csU9JmqD55vYQx/8ehwR+k1GIPRiRryEQiF1bQ+pm6DDlnHTTCouO8"
        + "wdgaf538zrDLvgGFCmXQxEXzD8W6mmqhutVooRIRHmMSPXWdI9lrmNWP18ZRv+cDts2cLn0LsIYjPEyMYLY7YfxCINJByMNGU27oac0VF/4A"
        + "YHBVsPBO856WpZr0wjj4u/M7baWmuM+ecZ32DqWMypkC8cfFbS+Z09I2TO4lvHj52dWoqVx8c3AbDIhg==";

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
    void shouldBeReadyToRunOnOrAfterStartTime(boolean migrationEnabled,
                                              int migrationStartHour,
                                              int migrationEndHour,
                                              boolean assertion) {
        ReflectionTestUtils.setField(underTest, "migrationStartHour", migrationStartHour);
        ReflectionTestUtils.setField(underTest, "migrationEndHour", migrationEndHour);
        ReflectionTestUtils.setField(underTest, "venueDataMigrationEnabled", migrationEnabled);

        assertEquals(underTest.readyToRun(), assertion);
    }

    @Test
    void shouldRunTheJob() {
        ReflectionTestUtils.setField(underTest, "isVenueRollback", false);
        ReflectionTestUtils.setField(underTest, "venueEncodedDataString", COMPRESSSED_ENCODED_DATA_STRING);

        underTest.run();

        verify(mockedAppender, times(4)).doAppend(logEventCaptor.capture());
        var capturedLogs = logEventCaptor.getAllValues();
        assertEquals("{} scheduler started : {}", capturedLogs.get(0).getMessage());
        assertEquals("Processing migration job", capturedLogs.get(1).getFormattedMessage());
        assertEquals("{} scheduler ended : {}", capturedLogs.get(2).getMessage());
        assertEquals("Case loader Shutting down...", capturedLogs.get(3).getFormattedMessage());
    }

    @ParameterizedTest
    @MethodSource("getRollbackScenarios")
    void shouldProcessTheJob(boolean isRollback, String languageColumn) throws IOException {
        ReflectionTestUtils.setField(underTest, "isVenueRollback", isRollback);
        ReflectionTestUtils.setField(underTest, "venueDataMigrationEnabled", true);
        ReflectionTestUtils.setField(underTest, "venueEncodedDataString", COMPRESSSED_ENCODED_DATA_STRING);
        underTest.process();

        verify(migrationService).process(eq(languageColumn), eq(underTest), eq(COMPRESSSED_ENCODED_DATA_STRING));
        verify(mockedAppender, atMostOnce()).doAppend(logEventCaptor.capture());
        String job = isRollback ? "rollback" : "migration";
        assertEquals(
            "Processing " + job + " job", logEventCaptor.getValue().getFormattedMessage()
        );
    }

    @Test
    void shouldProcessTheJob() throws IOException {
        doThrow(new IOException("Simulating decode failure"))
            .when(migrationService).process(anyString(), eq(underTest), eq(null));

        Exception exception = assertThrows(RuntimeException.class, () -> underTest.process());

        assertTrue(exception.getMessage().contains("Simulating decode failure"));
    }

    @ParameterizedTest
    @MethodSource("getInvalidStates")
    void shouldSkipDormantOrVoidCase(String state) {
        SscsCaseDetails caseDetails = SscsCaseDetails.builder().data(
                SscsCaseData.builder().processingVenue("South Shields").build())
            .state(state).build();
        boolean shouldSkip = underTest.shouldBeSkipped(caseDetails, caseDetails.getData().getProcessingVenue());
        assertTrue(shouldSkip);
    }

    @Test
    void shouldSkipIdenticalVenue() {
        SscsCaseDetails caseDetails = SscsCaseDetails.builder().data(
                SscsCaseData.builder().processingVenue("South Shields").build())
            .state("validAppeal")
            .build();
        boolean shouldSkip = underTest.shouldBeSkipped(caseDetails, caseDetails.getData().getProcessingVenue());
        assertTrue(shouldSkip);
    }

    @Test
    void shouldProcessDifferentVenue() {
        SscsCaseDetails caseDetails = SscsCaseDetails.builder().data(
                SscsCaseData.builder().processingVenue("South").build())
            .state("validAppeal")
            .build();
        boolean shouldSkip = underTest.shouldBeSkipped(caseDetails, "South Shields");
        assertTrue(!shouldSkip);
    }

    private static List<Arguments> getInvalidStates() {
        return List.of(
            Arguments.of("voidState"),
            Arguments.of("dormantState")
        );
    }

    private static List<Arguments> getStartHourScenarios() {
        return List.of(
            Arguments.of(false, now().getHour(), now().getHour() + 1, false),
            Arguments.of(false, now().getHour() - 1, now().getHour(),  false),
            Arguments.of(false, now().getHour() + 1,  now().getHour() + 2, false),
            Arguments.of(true, now().getHour(), now().getHour() + 1, true),
            Arguments.of(true, now().getHour() - 1, now().getHour() + 1, true),
            Arguments.of(true, now().getHour() - 2, now().getHour() - 1, false),
            Arguments.of(false, now().getHour() - 1, now().getHour() + 1, false),
            Arguments.of(true, now().getHour() + 1, now().getHour() + 2, false)
        );
    }

    private static List<Arguments> getRollbackScenarios() {
        return List.of(
            Arguments.of(true, EXISTING_DATA_COLUMN),
            Arguments.of(false, MAPPED_DATA_COLUMN)
        );
    }
}
