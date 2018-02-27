package uk.gov.hmcts.reform.sscs.refdataloader;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import javax.xml.stream.XMLStreamException;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.exceptions.Gaps2ReferenceDataNotFoundException;
import uk.gov.hmcts.reform.sscs.models.ReferenceDataFiles;
import uk.gov.hmcts.reform.sscs.models.refdata.AdminTeam;

public class AdminTeamReferenceDataLoaderTest {

    private AdminTeamReferenceDataLoader adminTeamReferenceDataLoader;

    @Before
    public void setUp() throws Exception {
        adminTeamReferenceDataLoader = new AdminTeamReferenceDataLoader();
    }

    @Test
    public void testToParseAndStoreAdminTeamIntoAMap() throws XMLStreamException {

        InputStream referenceDataResourceAsStream = ClassLoader
            .getSystemResourceAsStream(ReferenceDataFiles.GAPS2_REFERENCE_DATA_XML);

        adminTeamReferenceDataLoader.extract(referenceDataResourceAsStream);

        AdminTeam adminTeam = adminTeamReferenceDataLoader.getAdminTeamsById(1);

        assertThat(adminTeam.getAdminTeamId(), equalTo(1));
        assertThat(adminTeam.getAdmName(), equalTo("Barnsley"));
        assertThat(adminTeam.getVenueId(), equalTo(1));
        assertThat(adminTeam.getAdminTeamCode(), equalTo("SC001"));
        assertThat(adminTeam.getAdmClosureDate().toString(),
            equalTo("2009-01-01T16:41:10.073"));
    }

    @Test(expected = XMLStreamException.class)
    public void shouldThrowXmlStreamExceptionForInvalidXml() throws XMLStreamException {
        InputStream referenceDataResourceAsStream = ClassLoader
            .getSystemResourceAsStream(ReferenceDataFiles.INVALID_GAPS2_REFERENCE_DATA_XML);

        adminTeamReferenceDataLoader.extract(referenceDataResourceAsStream);

    }

    @Test(expected = Gaps2ReferenceDataNotFoundException.class)
    public void shouldReturnGaps2ReferenceDataNotFoundException() {
        adminTeamReferenceDataLoader.getAdminTeamsById(100);
    }
}
