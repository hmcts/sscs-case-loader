package uk.gov.hmcts.reform.sscs.refdata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.models.refdata.VenueDetails;

@RunWith(SpringRunner.class)
@SpringBootTest
public class VenueDataLoaderTest {

    @Autowired
    private VenueDataLoader venueDataLoader;

    @Test
    public void shouldLoadReferenceData() {
        Map<String, VenueDetails> venueDetailsMap = venueDataLoader.getVenueDetailsMap();

        assertNotNull(venueDetailsMap);
        assertEquals(256, venueDetailsMap.size());
    }
}
