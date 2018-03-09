package uk.gov.hmcts.reform.sscs.services;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.reform.sscs.services.gaps2.Gaps2FileLoader;
import uk.gov.hmcts.reform.sscs.services.gaps2.files.Gaps2File;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpSshService;

@RunWith(MockitoJUnitRunner.class)
public class Gaps2FileLoaderTest {

    private final Gaps2File delta1 = new Gaps2File("SSCS_Extract_Delta_2018-01-01-01-01-01.xml");
    private final Gaps2File delta2 = new Gaps2File("SSCS_Extract_Delta_2018-01-02-01-01-01.xml");
    private final Gaps2File delta3 = new Gaps2File("SSCS_Extract_Delta_2018-01-03-01-01-01.xml");

    private final Gaps2File ref1 = new Gaps2File("SSCS_Extract_Reference_2018-01-01-01-01-01.xml");
    private final Gaps2File ref2 = new Gaps2File("SSCS_Extract_Reference_2018-01-02-01-01-01.xml");
    private final Gaps2File ref3 = new Gaps2File("SSCS_Extract_Reference_2018-01-03-01-01-01.xml");

    @Mock
    private SftpSshService sftp;

    private final String ignoreCasesBeforeDateProperty = "2018-01-02";

    private Gaps2FileLoader loader;

    @Before
    public void setUp() {

        loader = new Gaps2FileLoader(ignoreCasesBeforeDateProperty, sftp);
    }

    @Test
    public void shouldLoadTheEarliestFileGivenAListOfFilesFromSftp() {
        List<Gaps2File> fileList1 = newArrayList(delta3, ref3, delta2, delta1, ref2, ref1);
        List<Gaps2File> fileList2 = newArrayList(delta3, ref3, delta2, delta1, ref1);
        List<Gaps2File> fileList3 = newArrayList(delta3, ref3, delta1, ref1);
        List<Gaps2File> fileList4 = newArrayList(delta3, delta1, ref1);
        List<Gaps2File> fileList5 = newArrayList(delta1, ref1);

        when(sftp.getFiles())
            .thenReturn(fileList1)
            .thenReturn(fileList2)
            .thenReturn(fileList3)
            .thenReturn(fileList4)
            .thenReturn(fileList5)
            .thenReturn(newArrayList());

        assertThat(loader.getNextFile(), is(ref2));
        assertThat(loader.getNextFile(), is(delta2));
        assertThat(loader.getNextFile(), is(ref3));
        assertThat(loader.getNextFile(), is(delta3));
    }

    @Test
    public void shouldMoveToProcessedGivenFileLoadedSuccessfully() {
        loader.processed(ref2);
        loader.processed(delta2);

        verify(sftp).move(ref2, true);
        verify(sftp).move(delta2, true);
    }

    @Test
    public void shouldMoveToFailedGivenFileLoadFailed() {
        loader.failed(ref2);
        loader.failed(delta2);

        verify(sftp).move(ref2, false);
        verify(sftp).move(delta2, false);
    }
}
