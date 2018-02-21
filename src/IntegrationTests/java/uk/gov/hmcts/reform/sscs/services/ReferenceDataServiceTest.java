package uk.gov.hmcts.reform.sscs.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import uk.gov.hmcts.reform.sscs.models.refdata.VenueDetails;
import uk.gov.hmcts.reform.sscs.refdataloader.ReferenceDataLoader;
import uk.gov.hmcts.reform.sscs.services.refdata.ReferenceDataService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ReferenceDataServiceTest {

    @Autowired
    private ReferenceDataLoader referenceDataLoader;

    private ReferenceDataService referenceDataService;

    @Before
    public void setUp() {
        referenceDataService = new ReferenceDataService(referenceDataLoader);
    }

    @Test
    public void givenVenueId_shouldReturnVenueDetails() {
        VenueDetails venueDetails = VenueDetails.builder()
            .venueId("2")
            .threeDigitReference("SC028")
            .regionalProcessingCentre("SSCS Birmingham")
            .venName("The Hilton Hotel")
            .venAddressLine1("Elton Way")
            .venAddressLine2("")
            .venAddressTown("Watford")
            .venAddressCounty("Hertfordshire")
            .venAddressPostcode("WD25 8HA")
            .venAddressTelNo("")
            .districtId("401")
            .url("https://www.google.com/maps/place/Hilton+Watford/@51.6619157,-0.3606389,"
                + "16.22z/data=!4m13!1m7!3m6!1s0x48761540757265d5:0xcef8473b5922e8fd!2sElton+Way,+Watford+WD25+8HH,"
                + "+UK!3b1!8m2!3d51.6618537!4d-0.3565498!3m4!1s0x4876044da1f6b35b:0x6f27a262bd6fd675!8m2!3d51.66515!4d"
                + "-0.3607142")
            .build();

        assertEquals(venueDetails, referenceDataService.getVenueDetails("2"));

    }

    @Test
    public void givenDifferentVenueIds_shouldReturnExpectedResponse() {
        assertNull(referenceDataService.getVenueDetails("xxxxx"));
        assertNull(referenceDataService.getVenueDetails(null));
    }
}
