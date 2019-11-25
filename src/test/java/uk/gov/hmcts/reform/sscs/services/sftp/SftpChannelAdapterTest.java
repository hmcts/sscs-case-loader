package uk.gov.hmcts.reform.sscs.services.sftp;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Vector;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.sscs.config.properties.SftpSshProperties;
import uk.gov.hmcts.reform.sscs.exceptions.SftpCustomException;
import uk.gov.hmcts.reform.sscs.services.gaps2.files.Gaps2File;

@RunWith(MockitoJUnitRunner.class)
public class SftpChannelAdapterTest {

    @Mock
    private JSch jsch;

    @Mock
    private Session session;

    @Mock
    private ChannelSftp channel;

    @Mock
    private SftpATTRS sftpattrs;

    @Mock
    private ChannelSftp.LsEntry entry;

    private final SftpSshProperties props = new SftpSshProperties();

    private static final String rname1 = "SSCS_Extract_Reference_2000-01-01-01-01-01.xml";
    private static final String rname2 = "SSCS_Extract_Reference_2000-01-01-01-01-02.xml";
    private static final String rname3 = "SSCS_Extract_Reference_2000-01-01-01-01-03.xml";
    private static final String dname1 = "SSCS_Extract_Delta_2000-01-01-01-01-01.xml";
    private static final String dname2 = "SSCS_Extract_Delta_2000-01-01-01-01-02.xml";
    private static final String dname3 = "SSCS_Extract_Delta_2000-01-01-01-01-03.xml";
    private SftpChannelAdapter sftp;

    @Before
    public void setUp() throws Exception {
        props.setHost("host");
        props.setInputDirectory("in");
        props.setKeyLocation("key");
        props.setPort(123);
        props.setUsername("user");

        when(jsch.getSession(
            "user",
            "host",
            123)).thenReturn(session);
        when(session.openChannel("sftp")).thenReturn(channel);
        when(channel.get("xxx")).thenReturn(new ByteArrayInputStream("abc".getBytes()));

        when(entry.getAttrs()).thenReturn(sftpattrs);

        sftp = new SftpChannelAdapter(jsch, props);
    }

    private void verifyConnection() throws JSchException, SftpException {
        verify(jsch).addIdentity("SSCS-SFTP", "key".getBytes(), null, null);
        verify(jsch).getSession("user", "host", 123);
        verify(session).setConfig("StrictHostKeyChecking", "no");
        verify(session).connect(60000);
        verify(session).openChannel("sftp");
        verify(channel).connect();
        verify(channel).cd("in");

    }

    @Test
    public void shouldCheckDirectoriesGivenInitialising() throws Exception {
        sftp.init();
        verify(channel).stat("processed/");
        verify(channel).stat("failed/");

        verifyConnection();
    }

    @Test
    public void shouldCreateDirectoriesGivenNoneSet() throws Exception {
        doThrow(new SftpException(1, "")).when(channel).stat("processed/");
        doThrow(new SftpException(1, "")).when(channel).stat("failed/");

        sftp.init();

        verify(channel).stat("processed/");
        verify(channel).stat("failed/");
        verify(channel).mkdir("processed/");
        verify(channel).mkdir("failed/");

        verifyConnection();
    }

    @Test(expected = SftpCustomException.class)
    public void shouldNotCreateDirectoriesGivenNoneSetAndThrowException() throws SftpException {
        doThrow(SftpException.class).when(channel).stat(anyString());
        doThrow(SftpException.class).when(channel).mkdir(anyString());

        sftp.init();

    }

    @Test(expected = SftpCustomException.class)
    public void shouldThrowExceptionOnInitializeJch() throws JSchException {
        doThrow(JSchException.class).when(jsch).addIdentity(any(), any(), any(), any());

        sftp.init();

    }

    @Test(expected = SftpCustomException.class)
    public void shouldThrowExceptionOnSessionConnect() throws JSchException {
        doThrow(JSchException.class).when(session).connect(anyInt());

        sftp.init();

    }

    @Test(expected = SftpCustomException.class)
    public void shouldThrowExceptionOnChannelConnect() throws JSchException {
        doThrow(JSchException.class).when(channel).connect();

        sftp.init();

    }

    @Test(expected = SftpCustomException.class)
    public void shouldThrowExceptionOnChannelCd() throws SftpException {
        doThrow(SftpException.class).when(channel).cd(anyString());

        sftp.init();

    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReturnListOfFilesGivenPath() throws Exception {
        List<ChannelSftp.LsEntry> lsEntries = newArrayList(entry, entry, entry, entry, entry, entry);

        when(entry.getFilename())
            .thenReturn(rname2)
            .thenReturn(dname1)
            .thenReturn(dname2)
            .thenReturn(dname3)
            .thenReturn(rname1)
            .thenReturn(rname3);

        when(channel.ls("*.xml")).thenReturn(new Vector(lsEntries)); //NOPMD

        List<Gaps2File> list = sftp.listIncoming();
        assertThat(list.size(), is(6));
        assertThat(list.get(0).getName(), is(rname1));
        assertThat(list.get(1).getName(), is(dname1));
        assertThat(list.get(2).getName(), is(rname2));
        assertThat(list.get(3).getName(), is(dname2));
        assertThat(list.get(4).getName(), is(rname3));
        assertThat(list.get(5).getName(), is(dname3));

        verify(channel).ls("*.xml");

        verifyConnection();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReturnFilesGivenProcessedFilesExists() throws Exception {
        List<ChannelSftp.LsEntry> lsEntries = newArrayList(entry, entry);

        when(entry.getFilename()).thenReturn(dname1).thenReturn(dname2);
        when(channel.ls("processed/*.xml")).thenReturn(new Vector(lsEntries)); //NOPMD

        List<Gaps2File> list = sftp.listProcessed();
        assertThat(list.size(), is(2));
        assertThat(list.get(0).getName(), is(dname1));
        assertThat(list.get(1).getName(), is(dname2));

        verify(channel).ls("processed/*.xml");

        verifyConnection();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReturnFileGivenFailedFileExists() throws Exception {
        List<ChannelSftp.LsEntry> lsEntries = newArrayList(entry);

        when(entry.getFilename()).thenReturn(dname1);
        when(channel.ls("failed/*.xml")).thenReturn(new Vector(lsEntries)); //NOPMD

        List<Gaps2File> list = sftp.listFailed();
        assertThat(list.size(), is(1));
        assertThat(list.get(0).getName(), is(dname1));

        verify(channel).ls("failed/*.xml");

        verifyConnection();
    }

    @Test(expected = SftpCustomException.class)
    public void shouldThrowSftpExceptionOnListFiles() throws Exception {

        doThrow(SftpException.class).when(channel).ls("failed/*.xml");

        sftp.listFailed();
    }

    @Test
    public void shouldReturnEmptyListIfThrowExceptionOnListFiles() throws Exception {

        doThrow(NullPointerException.class).when(channel).ls("failed/*.xml");

        List<Gaps2File> list = sftp.listFailed();
        assertTrue(list.isEmpty());
        verify(channel).ls("failed/*.xml");

        verifyConnection();
    }

    @Test
    public void shouldReturnInputStreamGivenAFileName() throws Exception {
        InputStream is = sftp.getInputStream("xxx");
        assertThat(IOUtils.toString(is, Charset.defaultCharset()), is("abc"));

        verify(channel).get("xxx");

        verifyConnection();
    }

    @Test(expected = SftpCustomException.class)
    public void shouldThrowExceptionGettingIsGivenGetFails() throws SftpException {
        doThrow(SftpException.class).when(channel).get(anyString());
        sftp.getInputStream("xxx");
    }

    @Test
    public void shouldMoveAFileGivenTheDestination() throws SftpException, JSchException {
        sftp.move(true, "xxx");
        verify(channel).connect();
        verify(channel).put(SftpChannelAdapter.DUMMY, "processed/xxx");
        verifyConnection();
    }

    @Test(expected = SftpCustomException.class)
    public void shouldThrowExceptionGivenMoveFails() throws Exception {
        doThrow(SftpException.class).when(channel).put(any(InputStream.class), any());
        sftp.move(true, "xxx");
    }

    @Test
    public void shouldCloseAllSessions() throws Exception {
        sftp.init();
        sftp.openConnectedChannel(2);

        sftp.close();

        verify(session).disconnect();
        verify(channel).disconnect();

    }

    @Test
    public void shouldReusableOpenConnectedChannel() throws Exception {
        sftp.init();

        sftp.openConnectedChannel(2);

        verify(jsch).addIdentity("SSCS-SFTP", "key".getBytes(), null, null);
        verify(jsch).getSession("user", "host", 123);
        verify(session).setConfig("StrictHostKeyChecking", "no");
        verify(session).connect(60000);
        verify(session, times(2)).openChannel("sftp");
        verify(channel, times(2)).connect();
        verify(channel).cd("in");
    }

}
