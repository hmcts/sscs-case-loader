package uk.gov.hmcts.reform.sscs.refdataloader;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import javax.xml.stream.XMLStreamException;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.exceptions.Gaps2ReferenceDataNotFoundException;
import uk.gov.hmcts.reform.sscs.models.ReferenceDataFiles;
import uk.gov.hmcts.reform.sscs.models.refdata.BenAssessType;

public class BenAssessTypeReferenceDataLoaderTest {

    private BenAssessTypeReferenceDataLoader benAssessTypeReferenceDataLoader;

    @Before
    public void setUp() throws Exception {
        benAssessTypeReferenceDataLoader = new BenAssessTypeReferenceDataLoader();
    }


    @Test
    public void testToParseAndStoreBenAssessTypeIntoAMap() throws XMLStreamException {

        InputStream referenceDataResourceAsStream = ClassLoader
            .getSystemResourceAsStream(ReferenceDataFiles.GAPS2_REFERENCE_DATA_XML);

        benAssessTypeReferenceDataLoader.extract(referenceDataResourceAsStream);

        BenAssessType benAssessType = benAssessTypeReferenceDataLoader.getBenAssessTypeById(1);

        assertThat(benAssessType.getBenAssessTypeId(), equalTo(1));
        assertThat(benAssessType.getBatCode(), equalTo("016"));
        assertThat(benAssessType.getBatDesc(), equalTo("CHILD BENEFIT/CHILD BENEFIT(LONE PARENT)"));
        assertThat(benAssessType.getBatRefPrefix(), equalTo("CF"));
        assertThat(benAssessType.getBatRefPrefixUt(), equalTo("CF"));
    }

    @Test(expected = XMLStreamException.class)
    public void shouldThrowXmlStreamExceptionForInvalidXml() throws XMLStreamException {
        InputStream referenceDataResourceAsStream = ClassLoader
            .getSystemResourceAsStream(ReferenceDataFiles.INVALID_GAPS2_REFERENCE_DATA_XML);

        benAssessTypeReferenceDataLoader.extract(referenceDataResourceAsStream);
    }

    @Test(expected = Gaps2ReferenceDataNotFoundException.class)
    public void shouldReturnGaps2ReferenceDataNotFoundException() {
        benAssessTypeReferenceDataLoader.getBenAssessTypeById(100);
    }
}
