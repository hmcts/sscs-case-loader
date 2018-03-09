package uk.gov.hmcts.reform.sscs.refdata;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKey.CASE_CODE;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKey.OFFICE;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKeyField.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.xml.stream.XMLStreamException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
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
    }
}
