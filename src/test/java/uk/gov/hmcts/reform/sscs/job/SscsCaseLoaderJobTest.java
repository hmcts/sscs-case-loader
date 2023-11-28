package uk.gov.hmcts.reform.sscs.job;

import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.sscs.services.CaseLoaderService;
import uk.gov.hmcts.reform.sscs.util.CaseLoaderTimerTask;

@ExtendWith(MockitoExtension.class)
class SscsCaseLoaderJobTest {

    @Mock
    CaseLoaderTimerTask timerTask;

    @Mock
    CaseLoaderService caseLoaderService;

    SscsCaseLoaderJob underTest;

    @BeforeEach
    void setUp() {
        underTest = new SscsCaseLoaderJob(caseLoaderService, timerTask);
    }

    @ParameterizedTest
    @MethodSource("getEndHourScenarios")
    void shouldBeReadyToRunOnOrAfterStartTime(int caseLoaderEndHour, boolean assertion) {
        ReflectionTestUtils.setField(underTest, "caseLoaderEndHour", caseLoaderEndHour);

        assertEquals(underTest.readyToRun(), assertion);
    }

    @Test
    void shouldProcessTheJob() {
        underTest.process();

        verify(caseLoaderService, atMostOnce()).process();
    }

    private static List<Arguments> getEndHourScenarios() {
        return List.of(
            Arguments.of(now().getHour(), true),
            Arguments.of(now().getHour() - 1, false),
            Arguments.of(now().getHour() + 1, true)
        );
    }
}
