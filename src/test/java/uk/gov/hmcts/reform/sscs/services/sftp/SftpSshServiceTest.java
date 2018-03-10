package uk.gov.hmcts.reform.sscs.services.sftp;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import com.jcraft.jsch.*;
import java.util.List;
import java.util.Vector;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.reform.sscs.config.properties.SftpSshProperties;
import uk.gov.hmcts.reform.sscs.exceptions.SftpCustomException;
import uk.gov.hmcts.reform.sscs.models.GapsInputStream;
import uk.gov.hmcts.reform.sscs.services.gaps2.files.Gaps2File;

@RunWith(MockitoJUnitRunner.class)
public class SftpSshServiceTest {

    public static final String DELTA_FILENAME = "SSCS_Extract_Delta_2017-05-24-16-14-19.xml";
    public static final String REF_FILENAME = "SSCS_Extract_Reference_2017-05-24-16-14-19.xml";
    public static final String INPUT_DIR = "incoming";
    public static final String USERNAME = "username";
    public static final String HOST = "host";
    public static final int PORT = 1234;
    @Mock
    private Session sesConnection;

    @Mock
    private JSch jschSshChannel;

    @Mock
    private ChannelSftp channelSftp;

    private final SftpSshProperties sftpSshProperties = new SftpSshProperties();

    private SftpSshService service;

    @Before
    public void setUp() throws JSchException {
        sftpSshProperties.setUsername(USERNAME);
        sftpSshProperties.setHost(HOST);
        sftpSshProperties.setPort(PORT);
        sftpSshProperties.setInputDirectory(INPUT_DIR);
        sftpSshProperties.setKeyLocation("key");

        stub(jschSshChannel.getSession(anyString(), anyString(), anyInt())).toReturn(sesConnection);
        stub(sesConnection.openChannel(anyString())).toReturn(channelSftp);

        service = new SftpSshService(jschSshChannel, sftpSshProperties);
    }

    @Test
    public void shouldReturnListOfFilesGivenFilesOnSftpServer() throws Exception {
        Vector<ChannelSftp.LsEntry> rows = new Vector<>();
        ChannelSftp.LsEntry row1 = mock(ChannelSftp.LsEntry.class);
        ChannelSftp.LsEntry row2 = mock(ChannelSftp.LsEntry.class);
        rows.add(row1);
        rows.add(row2);

        when(channelSftp.ls(INPUT_DIR + "/*.xml")).thenReturn(rows);
        when(row1.getFilename()).thenReturn(DELTA_FILENAME);
        when(row2.getFilename()).thenReturn(REF_FILENAME);

        List<Gaps2File> gaps2Files = service.getFiles();

        assertThat(gaps2Files.get(0).getName(), is(DELTA_FILENAME));
        assertTrue(gaps2Files.get(0).isDelta());
        assertThat(gaps2Files.get(1).getName(), is(REF_FILENAME));
        assertFalse(gaps2Files.get(1).isDelta());

        verify(jschSshChannel).addIdentity("SSCS-SFTP", sftpSshProperties.getKeyLocation().getBytes(),
            null, null);
    }

    @Test
    public void shouldMoveFileToProcessedDirectoryGivenSuccessfullyLoaded() throws SftpException {
        Gaps2File file = new Gaps2File(DELTA_FILENAME);
        service.move(file, true);

        verify(channelSftp).put("", INPUT_DIR + "/processed/" + DELTA_FILENAME);
    }

    @Test
    public void shouldMoveFileToFailedDirectoryGivenLoadFailed() throws SftpException {
        Gaps2File file = new Gaps2File(DELTA_FILENAME);
        service.move(file, false);

        verify(channelSftp).put("", INPUT_DIR + "/failed/" + DELTA_FILENAME);
    }

    private void mockSftpInternalServices(String fileName) throws Exception {
        Channel channelSftp = mock(ChannelSftp.class);
        Vector<ChannelSftp.LsEntry> rows = new Vector<>();
        ChannelSftp.LsEntry row = mock(ChannelSftp.LsEntry.class);
        rows.add(row);
        when(sesConnection.openChannel(anyString())).thenReturn(channelSftp);

        when(((ChannelSftp) channelSftp).ls(anyString())).thenReturn(rows);
        when(row.getFilename()).thenReturn(fileName);
    }

    @Test
    public void givenAListOfFilesInTheSftpServer_shouldGetDeltaFileAsInputStream() throws Exception {

        Object[][] params = {{DELTA_FILENAME, true, false},
                             {REF_FILENAME, false, true}};

        for (Object[] param : params) {
            mockSftpInternalServices((String) param[0]);

            List<GapsInputStream> result = service.readExtractFiles();

            assertThat(result, hasSize(1));
            assertTrue(result.get(0).getIsDelta() == param[1]);
            assertTrue(result.get(0).getIsReference() == param[2]);
        }
        verify(sesConnection, times(4)).connect(60000);
    }

    @Test(expected = SftpCustomException.class)
    public void givenSessionConnectFails_shouldThrowAnException() throws Exception {
        mockSftpInternalServices("Delta.xml");
        doThrow(JSchException.class).when(sesConnection).connect(anyInt());
        service.readExtractFiles();
    }

    @Test(expected = SftpCustomException.class)
    public void givenSessionOpenChannelFails_shouldThrowAnException() throws Exception {
        mockSftpInternalServices("Delta.xml");
        doThrow(JSchException.class).when(sesConnection).openChannel(anyString());
        service.readExtractFiles();
    }
}
