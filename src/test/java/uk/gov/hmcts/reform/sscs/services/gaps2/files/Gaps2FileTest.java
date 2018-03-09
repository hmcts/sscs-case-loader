package uk.gov.hmcts.reform.sscs.services.gaps2.files;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import org.junit.Before;
import org.junit.Test;

public class Gaps2FileTest {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");

    private final LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

    private String deltaFile;
    private String refFile;

    @Before
    public void setUp() {
        deltaFile = String.format("SSCS_Extract_Delta_%s.xml", formatter.format(now));
        refFile = String.format("SSCS_Extract_Reference_%s.xml", formatter.format(now));
    }

    @Test
    public void shouldReturnBooleanGivenExtractFileOfDifferentTypes() {

        assertTrue(new Gaps2File(deltaFile).isDelta());
        assertFalse(new Gaps2File(refFile).isDelta());
    }

    @Test
    public void shouldReturnDateGivenExtractFileName() {
        Gaps2File gaps2File = new Gaps2File(deltaFile);

        assertThat(gaps2File.getDate(), is(now));
    }
}
