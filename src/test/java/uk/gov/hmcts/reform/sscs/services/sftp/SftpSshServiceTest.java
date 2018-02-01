package uk.gov.hmcts.reform.sscs.services.sftp;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.reform.sscs.config.properties.SftpSshProperties;

import java.io.InputStream;
import java.util.List;
import java.util.Vector;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SftpSshServiceTest {

    @Mock
    private Session sesConnection;
    @Mock
    private JSch jschSshChannel;
    @Mock
    private SftpSshProperties sftpSshProperties;

    private SftpSshService service;

    @Before
    public void setUp() {
        service = new SftpSshService(jschSshChannel, sftpSshProperties);
    }

    @Test
    public void givenAListOfFiles_shouldGetFilesAsInputStream() throws SftpException, JSchException {
        Channel channelSftp = mock(ChannelSftp.class);

        Vector<ChannelSftp.LsEntry> rows = new Vector<>();
        ChannelSftp.LsEntry row = mock(ChannelSftp.LsEntry.class);
        rows.add(row);

        when(sesConnection.openChannel(anyString())).thenReturn(channelSftp);
        when(((ChannelSftp) channelSftp).ls(anyString())).thenReturn(rows);
        when(row.getFilename()).thenReturn("Testing.xml");

        List<InputStream> result = service.getFilesAsInputStreams(sesConnection);

        assertThat(result, hasSize(1));
    }

    @Test
    public void givenAConnectionRequest_shouldConnectToSftp() throws JSchException {
        when(jschSshChannel.getSession(anyString(), anyString(), anyInt())).thenReturn(sesConnection);
        doNothing().when(sesConnection).connect(anyInt());

        Session result = service.connect();

        assertEquals(result, sesConnection);

        verify(sesConnection).connect(60000);
    }

    @Test
    public void givenARequestToReadExtractFiles_shouldConnectToSftpAndReturnFilesAsInputStream()
        throws JSchException, SftpException {
        when(jschSshChannel.getSession(anyString(), anyString(), anyInt())).thenReturn(sesConnection);
        doNothing().when(sesConnection).connect(anyInt());

        Channel channelSftp = mock(ChannelSftp.class);

        Vector<ChannelSftp.LsEntry> rows = new Vector<>();
        ChannelSftp.LsEntry row = mock(ChannelSftp.LsEntry.class);
        rows.add(row);

        when(sesConnection.openChannel(anyString())).thenReturn(channelSftp);
        when(((ChannelSftp) channelSftp).ls(anyString())).thenReturn(rows);
        when(row.getFilename()).thenReturn("Testing.xml");

        List<InputStream> result = service.readExtractFiles();

        verify(sesConnection).connect(60000);

        assertThat(result, hasSize(1));
    }

    @Test
    public void shouldHandleJSchException() throws Exception {
        when(jschSshChannel.getSession(anyString(), anyString(), anyInt())).thenReturn(sesConnection);

        doThrow(new JSchException()).when(sesConnection).connect(anyInt());

        assertTrue(service.readExtractFiles().isEmpty());
    }

    @Test
    public void shouldHandleSftpException() throws Exception {
        when(jschSshChannel.getSession(anyString(), anyString(), anyInt())).thenReturn(sesConnection);

        Channel channelSftp = mock(ChannelSftp.class);

        when(sesConnection.openChannel(anyString())).thenReturn(channelSftp);

        doThrow(new SftpException(4, "")).when((ChannelSftp) channelSftp).ls(anyString());

        assertTrue(service.readExtractFiles().isEmpty());
    }
}
