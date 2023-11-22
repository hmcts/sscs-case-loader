package uk.gov.hmcts.reform.sscs.job;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
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
import uk.gov.hmcts.reform.sscs.util.CaseLoaderTimerTask;

import java.util.List;

import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DataMigrationJobTest {

    @Mock
    CaseLoaderTimerTask timerTask;

    @Mock
    private Appender<ILoggingEvent> mockedAppender;

    @Captor
    private ArgumentCaptor<LoggingEvent> logEventCaptor;

    DataMigrationJob underTest;

    @BeforeEach
    void setUp() {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.addAppender(mockedAppender);
        root.setLevel(Level.INFO);
        underTest = new DataMigrationJob(timerTask);
    }

    @ParameterizedTest
    @MethodSource("getStartTimeScenarios")
    void shouldBeReadyToRunOnOrAfterStartTime(boolean migrationEnabled, int caseLoaderStartTime, boolean assertion) {
        ReflectionTestUtils.setField(underTest, "caseLoaderStartTime", caseLoaderStartTime);
        ReflectionTestUtils.setField(underTest, "interpreterDataMigrationEnabled", migrationEnabled);

        assertEquals(underTest.readyToRun(), assertion);
    }

    @Test
    void shouldProcessTheJob() {
        underTest.process();

        verify(mockedAppender, atMostOnce()).doAppend(logEventCaptor.capture());
        assertEquals("Processing Interpreter data migration job", logEventCaptor.getValue().getMessage());
    }

    private static List<Arguments> getStartTimeScenarios() {
        return List.of(
            Arguments.of(false, now().getHour(), false),
            Arguments.of(false, now().getHour() - 1, false),
            Arguments.of(false, now().getHour() + 1, false),
            Arguments.of(true, now().getHour(), false),
            Arguments.of(true, now().getHour() - 1, false),
            Arguments.of(true, now().getHour() + 1, true)
        );
    }
}
