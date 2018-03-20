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

        referenceDataService = new ReferenceDataService(venueDataLoader);
    }

    @Test
    public void shouldReturnVenueGivenId() {
        assertThat(referenceDataService.getVenueDetails("123"), is(venueDetails));
    }

    @Test
    public void shouldReturnBenefitTypeGivenCaseCodeId() {
        when(refDataRepo.find(RefKey.CASE_CODE, "1", RefKeyField.BEN_ASSESS_TYPE_ID)).thenReturn("123");
        when(refDataRepo.find(RefKey.BEN_ASSESS_TYPE, "123", RefKeyField.BAT_CODE)).thenReturn("007");
        when(refDataRepo.find(RefKey.BAT_CODE_MAP, "007", RefKeyField.BENEFIT_DESC)).thenReturn("POP");

        referenceDataService.setRefDataRepo(refDataRepo);

        assertThat(referenceDataService.getBenefitType("1"), is("POP"));
    }

    @Test
    public void shouldReturnEvidenceTypeGivenTypeOfEvidenceId() {
        when(refDataRepo.find(RefKey.FUR_EVID_TYPE, "1", RefKeyField.FET_DESC)).thenReturn("Medical Evidence");

        referenceDataService.setRefDataRepo(refDataRepo);

        assertThat(referenceDataService.getEvidenceType("1"), is("Medical Evidence"));
    }

    @Test
    public void shouldReturnRoleTypeGivenRoleId() {
        when(refDataRepo.find(RefKey.PTTP_ROLE, "1", RefKeyField.PTR_DESC)).thenReturn("Appellant");

        referenceDataService.setRefDataRepo(refDataRepo);

        assertThat(referenceDataService.getRoleType("1"), is("Appellant"));
    }
}
