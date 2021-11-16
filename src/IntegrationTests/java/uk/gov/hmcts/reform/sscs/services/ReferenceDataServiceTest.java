package uk.gov.hmcts.reform.sscs.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.model.VenueDetails;
import uk.gov.hmcts.reform.sscs.refdata.RefDataRepository;
import uk.gov.hmcts.reform.sscs.service.VenueDataLoader;
import uk.gov.hmcts.reform.sscs.services.refdata.ReferenceDataService;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpChannelAdapter;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ReferenceDataServiceTest {

    @MockBean
    private SftpChannelAdapter channelAdapter;

    @Autowired
    private VenueDataLoader venueDataLoader;

    @Autowired
    private RefDataRepository refDataRepo;

    private ReferenceDataService referenceDataService;

    @Before
    public void setUp() {
        referenceDataService = new ReferenceDataService(venueDataLoader);
    }

    @Test
    public void givenVenueIdShouldReturnVenueDetails() {
        VenueDetails venueDetails = VenueDetails.builder()
            .venueId("43")
            .threeDigitReference("SC043")
            .regionalProcessingCentre("SSCS Bradford")
            .venName("Milton Keynes Magistrates Court")
            .venAddressLine1("300 Silbury Boulevard")
            .venAddressLine2("")
            .venAddressTown("Central Milton Keynes")
            .venAddressCounty("")
            .venAddressPostcode("MK9 2AJ")
            .venAddressTelNo("")
            .districtId("201")
            .url("https://goo.gl/maps/5nWYgZvnWGY2R3dV8")
            .active("Yes")
            .gapsVenName("Milton Keynes")
            .comments("")
            .build();

        assertEquals(venueDetails, referenceDataService.getVenueDetails("43"));

    }

    @Test
    public void givenDifferentVenueIdsShouldReturnExpectedResponse() {
        assertNull(referenceDataService.getVenueDetails("xxxxx"));
        assertNull(referenceDataService.getVenueDetails(""));
        assertNull(referenceDataService.getVenueDetails(null));
    }
}
