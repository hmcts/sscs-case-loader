package uk.gov.hmcts.reform.sscs.services.sftp;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jcraft.jsch.SftpException;
import java.io.InputStream;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.reform.sscs.services.gaps2.files.Gaps2File;

@RunWith(MockitoJUnitRunner.class)
public class SftpSshServiceTest {

    @Mock
    private SftpChannelAdapter channelAdapter;

    @Mock
    private Gaps2File file;

    @Mock
    private Gaps2File processed;

    @Mock
    private Gaps2File failed;

    @Mock
    private InputStream is;

    private SftpSshService service;

    @Before
    public void setUp() {
        when(channelAdapter.listProcessed()).thenReturn(newArrayList(processed));
        when(channelAdapter.listIncoming()).thenReturn(newArrayList(processed, file, file));
        when(file.getName()).thenReturn("xxx");
        when(channelAdapter.getInputStream("xxx")).thenReturn(is);

        service = new SftpSshService(channelAdapter);
    }

    @Test
    public void shouldReturnListOfFilesGivenFilesOnSftpServer() throws Exception {
        List<Gaps2File> gaps2Files = service.getFiles();

        assertThat(gaps2Files.get(0), is(file));
        assertThat(gaps2Files.get(1), is(file));
    }

    @Test
    public void shouldReturnStreamGivenGaps2File() throws Exception {
        assertThat(service.readExtractFile(file), is(is));
    }

    @Test
    public void shouldMoveFileToProcessedDirectoryGivenSuccessfullyLoaded() throws SftpException {
        service.move(file, true);
        verify(channelAdapter).move(true, "xxx");
    }

    @Test
    public void shouldMoveFileToFailedDirectoryGivenLoadFailed() throws SftpException {
        service.move(file, false);
        verify(channelAdapter).move(false, "xxx");
    }

    @Test
    public void shouldGetInputStreamGivenFileName() throws Exception {

        InputStream result = service.readExtractFile(file);
        assertThat(result, is(is));
    }
}
