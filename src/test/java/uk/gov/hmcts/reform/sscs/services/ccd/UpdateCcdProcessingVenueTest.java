package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.junit.Assert.*;

import junitparams.JUnitParamsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;

@RunWith(JUnitParamsRunner.class)
public class UpdateCcdProcessingVenueTest {
    UpdateCcdProcessingVenue classUnderTest = null;

    @Before
    public void init() {
        classUnderTest = new UpdateCcdProcessingVenue();
    }

    @Test
    public void returnFalseWhenNullGapsData() {
        assertFalse(classUnderTest.updateVenue(null, null));
    }

    @Test
    public void returnFalseWhenNullCcdData() {
        assertFalse(
            classUnderTest.updateVenue(
                SscsCaseData.builder().build(),
                null
            )
        );
    }

    @Test
    public void returnFalseWhenNoCcdDataOrGapsData() {
        assertFalse(
            classUnderTest.updateVenue(
                SscsCaseData.builder().build(),
                SscsCaseData.builder().build()
            )
        );
    }

    @Test
    public void returnTrueWhenCcdDataHasNullProcessingVenueName() {
        assertTrue(
            classUnderTest.updateVenue(
                SscsCaseData.builder().processingVenue("venue1").build(),
                SscsCaseData.builder().processingVenue(null).build()
            )
        );
    }

    @Test
    public void returnFalseWhenCcdDataHasSameProcessingVenue() {
        assertFalse(
            classUnderTest.updateVenue(
                SscsCaseData.builder()
                    .processingVenue("venue1")
                    .build(),
                SscsCaseData.builder()
                    .processingVenue("venue1")
                    .build()
            )
        );
    }

    @Test
    public void returnTrueWhenCcdDataHasDifferentProcessingVenue() {
        SscsCaseData gapsCaseData = SscsCaseData.builder()
            .processingVenue("Venue1")
            .build();

        SscsCaseData existingCcdCaseData = SscsCaseData.builder()
            .processingVenue("Venue2")
            .build();

        assertTrue(classUnderTest.updateVenue(gapsCaseData, existingCcdCaseData));
        assertEquals(gapsCaseData.getProcessingVenue(), existingCcdCaseData.getProcessingVenue());
    }

}
