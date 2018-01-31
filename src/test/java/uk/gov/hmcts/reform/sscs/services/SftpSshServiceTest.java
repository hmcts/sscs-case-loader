package uk.gov.hmcts.reform.sscs.services;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.reform.sscs.exceptions.SftpSshException;

import java.io.InputStream;
import java.util.List;
import java.util.Vector;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SftpSshService.class)
public class SftpSshServiceTest {

    @Mock
    private Session sesConnection;

    @Mock
    private JSch jschSshChannel;

    @InjectMocks
    @Autowired
    private SftpSshService service;

    @Test
    public void givenAListOfFiles_shouldGetFilesAsInputStream() throws SftpException, JSchException {
        Channel channelSftp = mock(ChannelSftp.class);

        Vector<ChannelSftp.LsEntry> rows = new Vector<>();
        ChannelSftp.LsEntry row = mock(ChannelSftp.LsEntry.class);
        rows.add(row);

        when(sesConnection.openChannel(anyString())).thenReturn(channelSftp);
        when(((ChannelSftp)channelSftp).ls(anyString())).thenReturn(rows);
        when((row).getFilename()).thenReturn("Testing.xml");

        List<InputStream> result = service.getFilesAsInputStreams(sesConnection);

        assertThat(result, hasSize(1));
    }

    @Test
    public void givenAConnectionRequest_shouldConnectToSftp() throws JSchException {
        when((jschSshChannel).getSession(anyString(), anyString(), anyInt())).thenReturn(sesConnection);
        doNothing().when(sesConnection).connect(anyInt());

        Session result = service.connect();

        assertEquals(result, sesConnection);

        verify(sesConnection).connect(60000);
    }

    @Test
    public void givenARequestToReadExtractFiles_shouldConnectToSftpAndReturnFilesAsInputStream()
        throws JSchException, SftpException, SftpSshException {
        when((jschSshChannel).getSession(anyString(), anyString(), anyInt())).thenReturn(sesConnection);
        doNothing().when(sesConnection).connect(anyInt());

        Channel channelSftp = mock(ChannelSftp.class);

        Vector<ChannelSftp.LsEntry> rows = new Vector<>();
        ChannelSftp.LsEntry row = mock(ChannelSftp.LsEntry.class);
        rows.add(row);

        when(sesConnection.openChannel(anyString())).thenReturn(channelSftp);
        when(((ChannelSftp)channelSftp).ls(anyString())).thenReturn(rows);
        when((row).getFilename()).thenReturn("Testing.xml");

        List<InputStream> result = service.readExtractFiles();

        verify(sesConnection).connect(60000);

        assertThat(result, hasSize(1));
    }

    @Test(expected = SftpSshException.class)
    public void shouldHandleJSchException() throws Exception {
        when((jschSshChannel).getSession(anyString(), anyString(), anyInt())).thenReturn(sesConnection);

        doThrow(new JSchException()).when(sesConnection).connect(anyInt());

        service.readExtractFiles();
    }

    @Test(expected = SftpSshException.class)
    public void shouldHandleSftpException() throws Exception {
        when((jschSshChannel).getSession(anyString(), anyString(), anyInt())).thenReturn(sesConnection);

        Channel channelSftp = mock(ChannelSftp.class);

        Vector<ChannelSftp.LsEntry> rows = new Vector<>();
        ChannelSftp.LsEntry row = mock(ChannelSftp.LsEntry.class);
        rows.add(row);

        when(sesConnection.openChannel(anyString())).thenReturn(channelSftp);

        doThrow(new SftpException(4, "")).when((ChannelSftp)channelSftp).ls(anyString());

        service.readExtractFiles();
    }
}
