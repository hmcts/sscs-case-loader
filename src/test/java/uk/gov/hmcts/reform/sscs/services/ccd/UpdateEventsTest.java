package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.reform.sscs.ccd.domain.Event;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;

@RunWith(JUnitParamsRunner.class)
public class UpdateEventsTest {
    private final UpdateEvents updateEvents = new UpdateEvents();

    @Test
    @Parameters(method = "generateEventUpdateScenarios")
    public void givenAnEventChange_shouldUpdateEventsInExistingCcdCase(SscsCaseData gapsCaseData,
                                                                       SscsCaseData existingCaseData,
                                                                       boolean expectedUpdate) {
        boolean update = updateEvents.update(gapsCaseData, existingCaseData);

        if (gapsCaseData != null && gapsCaseData.getEvents() != null
            && existingCaseData.getEvents() != null) {
            assertThat(gapsCaseData.getEvents().toArray(), equalTo(existingCaseData.getEvents().toArray()));
        }
        assertEquals(expectedUpdate, update);
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private Object[] generateEventUpdateScenarios() {
        SscsCaseData gapsCaseDataWithOneEvent = SscsCaseData.builder()
            .events(Collections.singletonList(Event.builder().build()))
            .build();

        SscsCaseData existingCaseDataWithTwoEvents = SscsCaseData.builder()
            .events(Arrays.asList(Event.builder().build(), Event.builder().build()))
            .build();

        SscsCaseData existingCaseDataWithOneEvents = SscsCaseData.builder()
            .events(Collections.singletonList(Event.builder().build()))
            .build();

        SscsCaseData sscsCaseDataWithNullEvent = SscsCaseData.builder()
            .events(null)
            .build();

        return new Object[]{
            new Object[]{sscsCaseDataWithNullEvent, existingCaseDataWithTwoEvents, false},
            new Object[]{gapsCaseDataWithOneEvent, sscsCaseDataWithNullEvent, true},
            new Object[]{gapsCaseDataWithOneEvent, existingCaseDataWithTwoEvents, true},
            new Object[]{gapsCaseDataWithOneEvent, existingCaseDataWithOneEvents, false},
            new Object[]{null, existingCaseDataWithOneEvents, false},
        };
    }

}
