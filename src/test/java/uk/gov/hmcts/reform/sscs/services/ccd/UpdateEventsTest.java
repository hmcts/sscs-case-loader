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
                                                                       SscsCaseData existingCaseDetails,
                                                                       boolean expectedUpdate) {
        boolean update = updateEvents.update(gapsCaseData, existingCaseDetails);

        if (gapsCaseData != null && gapsCaseData.getEvents() != null
            && existingCaseDetails.getEvents() != null) {
            assertThat(gapsCaseData.getEvents().toArray(), equalTo(existingCaseDetails.getEvents().toArray()));
        }
        assertEquals(expectedUpdate, update);
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private Object[] generateEventUpdateScenarios() {
        SscsCaseData gapsCaseDataWithOneEvent = SscsCaseData.builder()
            .events(Collections.singletonList(Event.builder().build()))
            .build();

        SscsCaseData existingCaseDetailsWithTwoScenarios = SscsCaseData.builder()
            .events(Arrays.asList(Event.builder().build(), Event.builder().build()))
            .build();

        SscsCaseData existingCaseDetailsOneScenarios = SscsCaseData.builder()
            .events(Collections.singletonList(Event.builder().build()))
            .build();

        SscsCaseData sscsCaseDataWithNullEvent = SscsCaseData.builder()
            .events(null)
            .build();

        return new Object[]{
            new Object[]{sscsCaseDataWithNullEvent, existingCaseDetailsWithTwoScenarios, false},
            new Object[]{gapsCaseDataWithOneEvent, sscsCaseDataWithNullEvent, true},
            new Object[]{gapsCaseDataWithOneEvent, existingCaseDetailsWithTwoScenarios, true},
            new Object[]{gapsCaseDataWithOneEvent, existingCaseDetailsOneScenarios, false},
            new Object[]{null, existingCaseDetailsOneScenarios, false}
        };
    }

}
