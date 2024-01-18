package uk.gov.hmcts.reform.sscs.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.sscs.job.DataMigrationJob.MAPPED_LANGUAGE_COLUMN;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
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
class DataMigrationServiceTest {

    private static final String ENCODED_DATA_STRING = "W3sicmVmZXJlbmNlIjoiMTcwMzAyMTkyNDYwMDQxOCIsIiI6IiIsImV2ZW50X"
        + "2lkIjoiIHZhbGlkQXBwZWFsQ3JlYXRlZCAiLCJleGlzdGluZ19sYW5ndWFnZV92YWx1ZSI6IiAgICAgICAgICAgICAgICAgICAgICAgICAg"
        + "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICIsIm1hcHBlZF9sYW5ndWFnZV92YWx1ZSI6IkFyYWJpYyIsInN1Ym1pdHRl"
        + "ZF9sYW5ndWFnZSI6IjIwNzI0NzEwNTAiLCJpbnRlcnByZXRlciI6IiBZZXMiLCJzdGF0ZSI6IiByZWFkeVRvTGlzdCAgICAgICIsIlVtYSBD"
        + "b21tZW50cyI6Ikxhbmd1YWdlIG5vdCBjYXB0dXJlZCBjb3JyZWN0bHkiLCJjYXNlX2RhdGFfaWQiOiIxMjM0NTY3In0seyJyZWZlcmVuY2Ui"
        + "OiIxNzAzMDIxOTgxODg4NjY2IiwiIjoiIiwiZXZlbnRfaWQiOiIgdmFsaWRBcHBlYWxDcmVhdGVkICIsImV4aXN0aW5nX2xhbmd1YWdlX3Zh"
        + "bHVlIjoiICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIiwibWFwcGVkX2xh"
        + "bmd1YWdlX3ZhbHVlIjoiQmVuZ2FsaSIsInN1Ym1pdHRlZF9sYW5ndWFnZSI6IiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg"
        + "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIiwiaW50ZXJwcmV0ZXIiOiIgWWVzIiwic3R"
        + "hdGUiOiIgaGVhcmluZyAgICAgICAgICAiLCJVbWEgQ29tbWVudHMiOiIiLCJjYXNlX2RhdGFfaWQiOiIxMjM0NTY4In1d";

    @Mock
    private CcdCasesSender ccdCasesSender;
    @Mock
    private IdamService idamService;
    @Mock
    private Appender<ILoggingEvent> mockedAppender;

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
    void shouldProcessCases() {
        ReflectionTestUtils.setField(underTest, "encodedDataString", ENCODED_DATA_STRING);
        IdamTokens tokens = IdamTokens.builder().build();
        when(idamService.getIdamTokens()).thenReturn(tokens);
        when(ccdCasesSender.updateLanguage(eq(1703021924600418L), eq(tokens), eq("Arabic")))
            .thenReturn(true);
        when(ccdCasesSender.updateLanguage(eq(1703021981888666L), eq(tokens), eq("Bengali")))
            .thenReturn(false);

        underTest.process(MAPPED_LANGUAGE_COLUMN);

        verify(ccdCasesSender).updateLanguage(1703021924600418L, tokens, "Arabic");
        verify(mockedAppender, times(2)).doAppend(logEventCaptor.capture());
        var capturedLogs = logEventCaptor.getAllValues();
        assertEquals("Number of cases to be migrated: (2)", capturedLogs.get(0).getFormattedMessage());
        assertEquals("Number of unprocessed cases: (1)", capturedLogs.get(1).getFormattedMessage());
    }
}
