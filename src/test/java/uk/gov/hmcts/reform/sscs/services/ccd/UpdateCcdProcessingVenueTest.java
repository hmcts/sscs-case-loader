package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import junitparams.JUnitParamsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.model.VenueDetails;
import uk.gov.hmcts.reform.sscs.service.VenueService;

@RunWith(JUnitParamsRunner.class)
public class UpdateCcdProcessingVenueTest {
    UpdateCcdProcessingVenue classUnderTest = null;

    @Mock
    private VenueService venueService;


    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
        classUnderTest = new UpdateCcdProcessingVenue(venueService);
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
        when(venueService.getEpimsIdForVenue(any())).thenReturn("12345");
        when(venueService.getVenueDetailsForActiveVenueByEpimsId("12345")).thenReturn(VenueDetails.builder().build());
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
    public void returnFalseWhenNewProcessingVenueHasLegacyVenueInCaseData() {
        when(venueService.getEpimsIdForVenue(any())).thenReturn("12345");
        when(venueService.getVenueDetailsForActiveVenueByEpimsId("12345"))
            .thenReturn(VenueDetails.builder().legacyVenue("venue2").build());

        assertFalse(
            classUnderTest.updateVenue(
                SscsCaseData.builder()
                    .processingVenue("venue1")
                    .build(),
                SscsCaseData.builder()
                    .processingVenue("venue2")
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

        when(venueService.getEpimsIdForVenue(any())).thenReturn("12345");
        when(venueService.getVenueDetailsForActiveVenueByEpimsId("12345")).thenReturn(VenueDetails.builder().build());
        assertTrue(classUnderTest.updateVenue(gapsCaseData, existingCcdCaseData));
        assertEquals(gapsCaseData.getProcessingVenue(), existingCcdCaseData.getProcessingVenue());
    }

}
