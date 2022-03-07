package uk.gov.hmcts.reform.sscs.refdata;

import static org.junit.Assert.assertNotNull;

import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.model.VenueDetails;
import uk.gov.hmcts.reform.sscs.service.VenueDataLoader;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpChannelAdapter;

@RunWith(SpringRunner.class)
@SpringBootTest
public class VenueDataLoaderTest {

    @MockBean
    SftpChannelAdapter channelAdapter;

    @Autowired
    private VenueDataLoader venueDataLoader;

    @Test
    public void shouldLoadReferenceData() {
        Map<String, VenueDetails> venueDetailsMap = venueDataLoader.getVenueDetailsMap();

        assertNotNull(venueDetailsMap);
    }
}
