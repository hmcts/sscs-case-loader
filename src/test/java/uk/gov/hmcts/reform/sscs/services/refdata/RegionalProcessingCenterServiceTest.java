package uk.gov.hmcts.reform.sscs.services.refdata;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.reform.sscs.models.refdata.RegionalProcessingCenter;

@RunWith(MockitoJUnitRunner.class)
public class RegionalProcessingCenterServiceTest {

    @Mock
    private AirLookupService airLookupService;

    private static final String SSCS_LIVERPOOL = "SSCS Liverpool";

    private RegionalProcessingCenterService regionalProcessingCenterService;

    @Before
    public void setUp() throws Exception {
        regionalProcessingCenterService = new RegionalProcessingCenterService(airLookupService);
    }

    @Test
    public void givenRpcMetaData_shouldLoadRpcMetadataToMap() {
        //When
        regionalProcessingCenterService.init();

        //Then
        Map<String, RegionalProcessingCenter> regionalProcessingCenterMap
                = regionalProcessingCenterService.getRegionalProcessingCenterMap();

        assertThat(regionalProcessingCenterMap.size(), equalTo(6));
        RegionalProcessingCenter regionalProcessingCenter = regionalProcessingCenterMap.get(SSCS_LIVERPOOL);
        assertThat(regionalProcessingCenter.getName(), equalTo("LIVERPOOL"));
        assertThat(regionalProcessingCenter.getAddress1(), equalTo("HM Courts & Tribunals Service"));
        assertThat(regionalProcessingCenter.getAddress2(), equalTo("Social Security & Child Support Appeals"));
        assertThat(regionalProcessingCenter.getAddress3(), equalTo("Prudential Buildings"));
        assertThat(regionalProcessingCenter.getAddress4(), equalTo("36 Dale Street"));
        assertThat(regionalProcessingCenter.getCity(), equalTo("LIVERPOOL"));
        assertThat(regionalProcessingCenter.getPostcode(), equalTo("L2 5UZ"));
        assertThat(regionalProcessingCenter.getPhoneNumber(), equalTo("0300 123 1142"));
        assertThat(regionalProcessingCenter.getFaxNumber(), equalTo("0870 324 0109"));

    }

    @Test
    public void getRegionalProcessingCentreFromVenueId() {
        regionalProcessingCenterService.init();

        String leedsVenueId = "10";
        RegionalProcessingCenter rpc = regionalProcessingCenterService.getByVenueId(leedsVenueId);

        assertThat(rpc.getName(), equalTo("LEEDS"));
    }

    @Test
    public void getRegionalProcessingCentreFromPostcode() {
        regionalProcessingCenterService.init();

        String somePostcode = "AB1 1AB";
        when(airLookupService.lookupRegionalCentre(somePostcode)).thenReturn("Leeds");
        RegionalProcessingCenter rpc = regionalProcessingCenterService.getByPostcode(somePostcode);

        assertThat(rpc.getName(), equalTo("LEEDS"));
    }
}
