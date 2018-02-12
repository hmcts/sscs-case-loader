package uk.gov.hmcts.reform.sscs.services.sftp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.util.List;
import java.util.Vector;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.sscs.config.properties.SftpSshProperties;
import uk.gov.hmcts.reform.sscs.exceptions.SftpCustomException;
import uk.gov.hmcts.reform.sscs.models.GapsInputStream;

@RunWith(JUnitParamsRunner.class)
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
        MockitoAnnotations.initMocks(this);
        service = new SftpSshService(jschSshChannel, sftpSshProperties);
    }

    private void mockSftpInternalServices(String fileName) throws Exception {
        when(jschSshChannel.getSession(anyString(), anyString(), anyInt())).thenReturn(sesConnection);
        Channel channelSftp = mock(ChannelSftp.class);
        Vector<ChannelSftp.LsEntry> rows = new Vector<>();
        ChannelSftp.LsEntry row = mock(ChannelSftp.LsEntry.class);
        rows.add(row);
        when(sesConnection.openChannel(anyString())).thenReturn(channelSftp);

        when(((ChannelSftp) channelSftp).ls(anyString())).thenReturn(rows);
        when(row.getFilename()).thenReturn(fileName);
    }

    @Test
    @Parameters({"SSCS_Extract_Delta_bla.xml, True, False", "SSCS_Extract_Reference_bla.xml, False, True"})
    public void givenAListOfFilesInTheSftpServer_shouldGetDeltaFileAsInputStream(
        String fileName, Boolean isDelta, Boolean isRef) throws Exception {

        mockSftpInternalServices(fileName);

        List<GapsInputStream> result = service.readExtractFiles();

        assertThat(result, hasSize(1));
        assertTrue(result.get(0).getIsDelta() == isDelta);
        assertTrue(result.get(0).getIsReference() == isRef);
        verify(sesConnection).connect(60000);
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
