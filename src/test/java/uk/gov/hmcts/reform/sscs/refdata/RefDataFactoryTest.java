package uk.gov.hmcts.reform.sscs.refdata;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKey.*;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKeyField.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.xml.stream.XMLStreamException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.sscs.services.refdata.ReferenceDataService;

@RunWith(MockitoJUnitRunner.class)
public class RefDataFactoryTest {

    @Mock
    private ReferenceDataService referenceDataService;

    private RefDataFactory factory;
    private InputStream is;

    private final String refData = "<?xml version=\"1.0\" standalone=\"yes\"?>\n"
        + "<Reference_Tables>\n"
        + "  <Case_Code>\n"
        + "    <CASE_CODE_ID>1</CASE_CODE_ID>\n"
        + "    <CCD_KEY>A</CCD_KEY>\n"
        + "  </Case_Code>\n"
        + "  <Office>\n"
        + "      <OFFICE_ID>1959</OFFICE_ID>\n"
        + "      <OFF_NAME>Liverpool</OFF_NAME>\n"
        + "  </Office>"
        + "  <Tribunal_Type>\n"
        + "    <TRIBUNAL_TYPE_ID>1</TRIBUNAL_TYPE_ID>\n"
        + "    <TBT_CODE>P</TBT_CODE>\n"
        + "    <TBT_DESC>Paper</TBT_DESC>\n"
        + "    <ROW_IS_DELETED>N</ROW_IS_DELETED>\n"
        + "    <LAST_MODIFIED_DATE>2009-01-01T16:41:12.14+00:00</LAST_MODIFIED_DATE>\n"
        + "    <LAST_MODIFIED_BY>SYSTEM</LAST_MODIFIED_BY>\n"
        + "    <TIMESTAMP>AAAAARGBC+w=</TIMESTAMP>\n"
        + "  </Tribunal_Type>\n"
        + "</Reference_Tables>\n";

    @Before
    public void setUp() {
        is = new ByteArrayInputStream(refData.getBytes());
        factory = new RefDataFactory(referenceDataService);
    }

    @Test
    public void shouldLoadReferenceDataGivenInputStream() throws XMLStreamException {

        factory.extract(is);

        ArgumentCaptor<RefDataRepository> capture = ArgumentCaptor.forClass(RefDataRepository.class);

        verify(referenceDataService).setRefDataRepo(capture.capture());

        RefDataRepository repo = capture.getValue();

        assertThat(repo.find(CASE_CODE, "1", CASE_CODE_ID), is("1"));
        assertThat(repo.find(CASE_CODE, "1", CCD_KEY), is("A"));

        assertThat(repo.find(OFFICE, "1959", OFFICE_ID), is("1959"));
        assertThat(repo.find(OFFICE, "1959", OFF_NAME), is("Liverpool"));
        assertThat(repo.find(TRIBUNAL_TYPE, "1", TBT_CODE), is("P"));
        assertThat(repo.find(BAT_CODE_MAP, "001", BENEFIT_DESC), is("UC"));
        assertThat(repo.find(BAT_CODE_MAP, "002", BENEFIT_DESC), is("PIP"));
        assertThat(repo.find(BAT_CODE_MAP, "051", BENEFIT_DESC), is("ESA"));
        assertThat(repo.find(BAT_CODE_MAP, "037", BENEFIT_DESC), is("DLA"));
        assertThat(repo.find(BAT_CODE_MAP, "070", BENEFIT_DESC), is("carersAllowance"));

    }
}
