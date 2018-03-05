package uk.gov.hmcts.reform.sscs.services.refdata;

import static com.google.common.collect.Maps.newHashMap;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.reform.sscs.models.refdata.VenueDetails;
import uk.gov.hmcts.reform.sscs.refdata.RefDataRepository;
import uk.gov.hmcts.reform.sscs.refdata.VenueDataLoader;
import uk.gov.hmcts.reform.sscs.refdata.domain.RefKey;
import uk.gov.hmcts.reform.sscs.refdata.domain.RefKeyField;

@RunWith(MockitoJUnitRunner.class)
public class ReferenceDataServiceTest {

    @Mock
    private VenueDataLoader venueDataLoader;

    @Mock
    private RefDataRepository refDataRepo;

    private ReferenceDataService referenceDataService;

    private VenueDetails venueDetails;

    @Before
    public void setUp() {
        Map<String, VenueDetails> venueMap = newHashMap();
        venueDetails = VenueDetails.builder().build();
        venueMap.put("123", venueDetails);
        when(venueDataLoader.getVenueDetailsMap()).thenReturn(venueMap);

        referenceDataService = new ReferenceDataService(venueDataLoader, refDataRepo);
    }

    @Test
    public void shouldReturnVenueGivenId() {
        assertThat(referenceDataService.getVenueDetails("123"), is(venueDetails));
    }

    @Test
    public void shouldReturnFieldGivenRefTypeAndKey() {
        when(refDataRepo.find(RefKey.CASE_CODE, RefKeyField.APS_MINOR)).thenReturn("ABC");

        assertThat(referenceDataService.getRefField(RefKey.CASE_CODE, RefKeyField.APS_MINOR), is("ABC"));
    }
}
