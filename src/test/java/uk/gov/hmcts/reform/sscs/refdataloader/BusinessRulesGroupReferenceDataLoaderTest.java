package uk.gov.hmcts.reform.sscs.refdataloader;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import javax.xml.stream.XMLStreamException;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.sscs.exceptions.Gaps2ReferenceDataNotFoundException;
import uk.gov.hmcts.reform.sscs.models.ReferenceDataFiles;
import uk.gov.hmcts.reform.sscs.models.refdata.BusinessRulesGroup;

public class BusinessRulesGroupReferenceDataLoaderTest {

    private BusinessRulesGroupReferenceDataLoader businessRulesGroupReferenceDataLoader;

    @Before
    public void setUp() throws Exception {
        businessRulesGroupReferenceDataLoader = new BusinessRulesGroupReferenceDataLoader();
    }

    @Test
    public void testToParseAndStorBusinessRulesGroupIntoAMap() throws XMLStreamException {

        InputStream referenceDataResourceAsStream = ClassLoader
            .getSystemResourceAsStream(ReferenceDataFiles.GAPS2_REFERENCE_DATA_XML);

        businessRulesGroupReferenceDataLoader.extract(referenceDataResourceAsStream);

        BusinessRulesGroup businessRulesGroup = businessRulesGroupReferenceDataLoader.getBusinessRulesGroupById(4);

        assertThat(businessRulesGroup.getBusinessRulesGrpId(), equalTo(4));
        assertThat(businessRulesGroup.getBrgShortDesc(), equalTo("CHILD SUPP"));
        assertThat(businessRulesGroup.getBrgLongDesc(), equalTo("DMA Child Support Group"));
        assertThat(businessRulesGroup.getTribunalId(), equalTo(1));
        assertThat(businessRulesGroup.getJurisdictionId(), equalTo(1));
    }

    @Test(expected = XMLStreamException.class)
    public void shouldThrowXmlStreamExceptionForInvalidXml() throws XMLStreamException {
        InputStream referenceDataResourceAsStream = ClassLoader
            .getSystemResourceAsStream(ReferenceDataFiles.INVALID_GAPS2_REFERENCE_DATA_XML);

        businessRulesGroupReferenceDataLoader.extract(referenceDataResourceAsStream);
    }

    @Test(expected = Gaps2ReferenceDataNotFoundException.class)
    public void shouldReturnGaps2ReferenceDataNotFoundException() {
        businessRulesGroupReferenceDataLoader.getBusinessRulesGroupById(100);
    }

}
