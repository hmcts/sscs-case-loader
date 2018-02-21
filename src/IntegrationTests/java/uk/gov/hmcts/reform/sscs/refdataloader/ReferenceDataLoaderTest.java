package uk.gov.hmcts.reform.sscs.refdataloader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import uk.gov.hmcts.reform.sscs.models.refdata.VenueDetails;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ReferenceDataLoaderTest {

    @Autowired
    private ReferenceDataLoader referenceDataLoader;

    @Test
    public void shouldLoadReferenceData() {
        List<VenueDetails> venueDetailsList = referenceDataLoader.getVenueDetailsList();

        assertNotNull(venueDetailsList);
        assertEquals(265, venueDetailsList.size());
    }
}
