package uk.gov.hmcts.reform.sscs.services;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import uk.gov.hmcts.reform.sscs.refdataloaders.GoogleMapRefDataLoader;
import uk.gov.hmcts.reform.sscs.services.refdata.ReferenceDataService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ReferenceDataServiceTest {

    @Autowired
    private GoogleMapRefDataLoader googleMapRefDataLoader;

    private ReferenceDataService referenceDataService;

    @Before
    public void setUp() {
        referenceDataService = new ReferenceDataService(googleMapRefDataLoader);
    }

    @Test
    public void givenVenueId_shouldReturnTheGoogleMapUrl() {
        final String googleMapUrl = "https://www.google.com/maps/place/Hilton+Watford/@51.6619157,-0.3606389,"
            + "16.22z/data=!4m13!1m7!3m6!1s0x48761540757265d5:0xcef8473b5922e8fd!2sElton+Way,+Watford+WD25+8HH,"
            + "+UK!3b1!8m2!3d51.6618537!4d-0.3565498!3m4!1s0x4876044da1f6b35b:0x6f27a262bd6fd675!8m2!3d51.66515!4d"
            + "-0.3607142";

        assertEquals(googleMapUrl, referenceDataService.getGoogleMapUrl("2"));

    }
}
