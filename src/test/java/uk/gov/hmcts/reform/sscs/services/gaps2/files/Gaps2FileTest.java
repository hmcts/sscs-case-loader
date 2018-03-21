package uk.gov.hmcts.reform.sscs.services.gaps2.files;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class Gaps2FileTest {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");

    private final LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

    private Gaps2File deltaFile;
    private Gaps2File refFile;

    @Before
    public void setUp() {
        deltaFile = getDeltaFile(now);
        refFile = getReferenceFile(now);
    }

    private Gaps2File getDeltaFile(LocalDateTime timestamp) {
        return getFile("SSCS_Extract_Delta_%s.xml", timestamp);
    }

    private Gaps2File getReferenceFile(LocalDateTime timestamp) {
        return getFile("SSCS_Extract_Reference_%s.xml", timestamp);
    }

    private Gaps2File getFile(String name, LocalDateTime timestamp) {
        String fileName = String.format(name, formatter.format(timestamp));
        return new Gaps2File(fileName);
    }

    @Test
    public void shouldOrderFilesByDateGivenRefAndDeltaFiles() {
        Gaps2File ref1 = getReferenceFile(now.minusDays(1));
        Gaps2File ref2 = getReferenceFile(now);
        Gaps2File ref3 = getReferenceFile(now.plusDays(1));
        Gaps2File delta1 = getReferenceFile(now.minusDays(1));
        Gaps2File delta2 = getReferenceFile(now);
        Gaps2File delta3 = getReferenceFile(now.plusDays(1));

        List<Gaps2File> files = newArrayList(delta3, delta2, delta1, ref3, ref2, ref1);
        Collections.sort(files);

        assertThat(files.get(0), is(ref1));
        assertThat(files.get(1), is(delta1));
        assertThat(files.get(2), is(ref2));
        assertThat(files.get(3), is(delta2));
        assertThat(files.get(4), is(ref3));
        assertThat(files.get(5), is(delta3));

        assertTrue(delta1.equals(delta1));
        assertNotNull(delta1);

        assertThat(delta2.hashCode(), is(delta2.getName().hashCode()));
    }

    @Test
    public void shouldReturnBooleanGivenExtractFileOfDifferentTypes() {

        assertTrue(deltaFile.isDelta());
        assertFalse(refFile.isDelta());
    }

    @Test
    public void shouldReturnDateGivenExtractFileName() {
        assertThat(deltaFile.getDate(), is(now));
    }
}
