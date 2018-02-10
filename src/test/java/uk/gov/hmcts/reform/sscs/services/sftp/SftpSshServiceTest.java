package uk.gov.hmcts.reform.sscs.services.sftp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;
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
import uk.gov.hmcts.reform.sscs.models.GapsInputStream;

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
    public void givenAListOfFiles_shouldGetDeltaFileAsInputStream() throws Exception {
        Channel channelSftp = mock(ChannelSftp.class);

        Vector<ChannelSftp.LsEntry> rows = new Vector<>();
        ChannelSftp.LsEntry row = mock(ChannelSftp.LsEntry.class);
        rows.add(row);

        when(sesConnection.openChannel(anyString())).thenReturn(channelSftp);
        when(((ChannelSftp) channelSftp).ls(anyString())).thenReturn(rows);
        when(row.getFilename()).thenReturn("SSCS_Extract_Delta_bla.xml");

        List<GapsInputStream> result = service.getFilesAsInputStreams(sesConnection);

        assertThat(result, hasSize(1));
        assertTrue(result.get(0).getIsDelta());
        assertFalse(result.get(0).getIsReference());
    }

    @Test
    public void givenAListOfFiles_shouldGetReferenceFileAsInputStream() throws Exception {
        Channel channelSftp = mock(ChannelSftp.class);

        Vector<ChannelSftp.LsEntry> rows = new Vector<>();
        ChannelSftp.LsEntry row = mock(ChannelSftp.LsEntry.class);
        rows.add(row);

        when(sesConnection.openChannel(anyString())).thenReturn(channelSftp);
        when(((ChannelSftp) channelSftp).ls(anyString())).thenReturn(rows);
        when(row.getFilename()).thenReturn("SSCS_Extract_Reference_bla.xml");

        List<GapsInputStream> result = service.getFilesAsInputStreams(sesConnection);

        assertThat(result, hasSize(1));
        assertTrue(result.get(0).getIsReference());
        assertFalse(result.get(0).getIsDelta());
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

        List<GapsInputStream> result = service.readExtractFiles();

        verify(sesConnection).connect(60000);

        assertThat(result, hasSize(1));
    }

}
