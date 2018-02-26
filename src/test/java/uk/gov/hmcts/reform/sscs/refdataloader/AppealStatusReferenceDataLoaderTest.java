package uk.gov.hmcts.reform.sscs.refdataloader;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import javax.xml.stream.XMLStreamException;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.models.refdata.AppealStatus;

public class AppealStatusReferenceDataLoaderTest {

    public static final String GAPS2_REFERENCE_DATA_XML =
        "reference-data/SSCS_Extract_ReferenceTest_2017-06-30-09-01-31.xml";
    private AppealStatusReferenceDataLoader appealStatusReferenceDataLoader;

    @Before
    public void setUp() throws Exception {
        appealStatusReferenceDataLoader = new AppealStatusReferenceDataLoader();
    }

    @Test
    public void testToParseAndStoreAppealStatusIntoAMap() throws XMLStreamException {

        InputStream referenceDataResourceAsStream = ClassLoader
            .getSystemResourceAsStream(GAPS2_REFERENCE_DATA_XML);

        appealStatusReferenceDataLoader.extract(referenceDataResourceAsStream);

        AppealStatus appealStatus = appealStatusReferenceDataLoader.getAppealStatusById(1);

        assertThat(appealStatus.getAppealStatusId(), equalTo(1));
        assertThat(appealStatus.getApsMinor(), equalTo(0));
        assertThat(appealStatus.getApsBfDays(), equalTo(730));
        assertThat(appealStatus.getApsDesc(), equalTo("DORMANT INVALID APPEAL, NOT DULY MADE"));
        assertThat(appealStatus.getApsDormant(), equalTo("Y"));
        assertThat(appealStatus.getApsSearch(), equalTo("Y"));

    }

    @Test(expected = XMLStreamException.class)
    public void shouldThrowXmlStreamExceptionForInvalidXml() throws XMLStreamException {
        InputStream referenceDataResourceAsStream = ClassLoader
            .getSystemResourceAsStream("reference-data/Invalid_SSCS_Extract_Reference.xml");

        appealStatusReferenceDataLoader.extract(referenceDataResourceAsStream);

    }


}
