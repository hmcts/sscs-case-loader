package uk.gov.hmcts.reform.sscs.refdata;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKey.CASE_CODE;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKeyField.CASE_CODE_ID;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.xml.stream.XMLStreamException;
import org.junit.Before;
import org.junit.Test;

public class RefDataFactoryTest {

    private RefDataRepository repo = new RefDataRepository();
    private RefDataFactory factory;
    private InputStream is;

    @Before
    public void setUp() {
        is = new ByteArrayInputStream(refData.getBytes());
        factory = new RefDataFactory(repo);
    }

    @Test
    public void shouldLoadReferenceDataGivenInptStream() throws XMLStreamException {

        factory.extract(is);

        assertThat(repo.find(CASE_CODE, CASE_CODE_ID), is("1"));
    }

    private String refData = "<?xml version=\"1.0\" standalone=\"yes\"?>\n"
        + "<Reference_Tables>\n"
        + "  <Case_Code>\n"
        + "    <CASE_CODE_ID>1</CASE_CODE_ID>\n"
        + "  </Case_Code>\n"
        + "</Reference_Tables>\n"
        + "";
}
