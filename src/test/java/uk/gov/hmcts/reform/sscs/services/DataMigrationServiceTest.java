package uk.gov.hmcts.reform.sscs.services;


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
import uk.gov.hmcts.reform.sscs.services.ccd.CcdCasesSender;


@ExtendWith(MockitoExtension.class)
class DataMigrationServiceTest {

    private static final String COMPRESSSED_ENCODED_DATA_STRING = "eJzNUT1PwzAQ/SuW5w52mjqGrXTtCANCKLraR7AUO5F9qahQ/z"
        + "sXgRBf7dIFDyfZfvfuvXcPrzLjE2ZMDuW11I1aqkpfVbVRqtZWLiS/csU9JmqD55vYQx/8ehwR+k1GIPRiRryEQiF1bQ+pm6DDlnHTTCouO8"
        + "wdgaf538zrDLvgGFCmXQxEXzD8W6mmqhutVooRIRHmMSPXWdI9lrmNWP18ZRv+cDts2cLn0LsIYjPEyMYLY7YfxCINJByMNGU27oac0VF/4A"
        + "YHBVsPBO856WpZr0wjj4u/M7baWmuM+ecZ32DqWMypkC8cfFbS+Z09I2TO4lvHj52dWoqVx8c3AbDIhg==";

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
    void shouldProcessCases() throws IOException {
    }
}
