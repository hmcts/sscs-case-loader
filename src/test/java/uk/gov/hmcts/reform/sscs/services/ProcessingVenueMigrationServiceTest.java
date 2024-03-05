package uk.gov.hmcts.reform.sscs.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.sscs.job.ProcessingVenueMigrationJob.MAPPED_VENUE_COLUMN;

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
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.services.ccd.CcdCasesSender;

@ExtendWith(MockitoExtension.class)
class ProcessingVenueMigrationServiceTest {

    private static final String COMPRESSSED_ENCODED_DATA_STRING = "eJyLrlYqSk1LLUrNS05VslIyNLM0MzY2MzA2NDY1NTAxUNJRy"
        + "k0sKEhNiS9LzStNjS9LzCkFqQvOLy3JUAjOyEzNSSkGKkqtyCwuycxLx6ZMqVYH0xJTQ1MjIxNzcyNjS5yWOCcWpWSmpeE2HqagNhYANttC"
        + "Fw==";

    @Mock
    private CcdCasesSender ccdCasesSender;
    @Mock
    private IdamService idamService;
    @Mock
    private Appender<ILoggingEvent> mockedAppender;

    @Captor
    private ArgumentCaptor<LoggingEvent> logEventCaptor;

    ProcessingVenueMigrationService underTest;

    @BeforeEach
    void setUp() {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.addAppender(mockedAppender);
        root.setLevel(Level.INFO);
        underTest = new ProcessingVenueMigrationService(ccdCasesSender, idamService);
    }

    @Test
    void shouldProcessCases() throws IOException {
        ReflectionTestUtils.setField(underTest, "encodedDataString", COMPRESSSED_ENCODED_DATA_STRING);
        IdamTokens tokens = IdamTokens.builder().build();
        when(idamService.getIdamTokens()).thenReturn(tokens);
        when(ccdCasesSender.updateProcessingVenue(eq(1696336031355040L), eq(tokens), eq("South Shields")))
            .thenReturn(true);
        when(ccdCasesSender.updateProcessingVenue(eq(1696515224772390L), eq(tokens), eq("Cardiff")))
            .thenReturn(false);

        underTest.process(MAPPED_VENUE_COLUMN);

        verify(ccdCasesSender).updateProcessingVenue(1696336031355040L, tokens, "South Shields");
        verify(mockedAppender, times(2)).doAppend(logEventCaptor.capture());
        var capturedLogs = logEventCaptor.getAllValues();
        assertEquals("Number of cases to be migrated: (2)", capturedLogs.get(0).getFormattedMessage());
        assertEquals("Number of unprocessed cases: (1)", capturedLogs.get(1).getFormattedMessage());
    }
}
