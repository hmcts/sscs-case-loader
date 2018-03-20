package uk.gov.hmcts.reform.sscs.services.sftp;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Vector;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
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
    private ChannelSftp.LsEntry entry;

    private final SftpSshProperties props = new SftpSshProperties();

    private static final String fname1 = "SSCS_Extract_Delta_2000-01-01-01-01-01.xml";
    private static final String fname2 = "SSCS_Extract_Delta_2000-01-01-01-01-02.xml";
    private static final String fname3 = "SSCS_Extract_Delta_2000-01-01-01-01-03.xml";
    private SftpChannelAdapter sftp;

    @Before
    public void setUp() throws Exception {
        props.setHost("host");
        props.setInputDirectory("in");
        props.setKeyLocation("key");
        props.setPort(123);
        props.setUsername("user");

        stub(jsch.getSession(
            "user",
            "host",
            123)).toReturn(session);
        stub(session.openChannel("sftp")).toReturn(channel);
        stub(channel.get("xxx")).toReturn(new ByteArrayInputStream("abc".getBytes()));

        sftp = new SftpChannelAdapter(jsch, props);
    }

    @After
    public void tearDown() throws JSchException, SftpException {
        verify(jsch).addIdentity("SSCS-SFTP", "key".getBytes(), null, null);
        verify(jsch).getSession("user", "host", 123);
        verify(session).setConfig("StrictHostKeyChecking", "no");
        verify(session).connect(60000);
        verify(session).openChannel("sftp");
        verify(channel).connect();
        verify(channel).cd("in");

        verifyNoMoreInteractions(jsch, session, channel);
    }

    @Test
    public void shouldCheckDirectoriesGivenInitialising() throws SftpException {
        sftp.init();
        verify(channel).stat("processed/");
        verify(channel).stat("failed/");
    }

    @Test
    public void shouldCreateDirectoriesGivenNoneSet() throws SftpException {
        doThrow(new SftpException(1, "")).when(channel).stat("processed/");
        doThrow(new SftpException(1, "")).when(channel).stat("failed/");

        sftp.init();

        verify(channel).stat("processed/");
        verify(channel).stat("failed/");
        verify(channel).mkdir("processed/");
        verify(channel).mkdir("failed/");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReturnListOfFilesGivenPath() throws SftpException {
        List<ChannelSftp.LsEntry> lsEntries = newArrayList(entry, entry, entry);

        when(entry.getFilename()).thenReturn(fname1)
            .thenReturn(fname2)
            .thenReturn(fname3);
        when(channel.ls("*.xml")).thenReturn(new Vector(lsEntries)); //NOPMD

        List<Gaps2File> list = sftp.listIncoming();
        assertThat(list.size(), is(3));
        assertThat(list.get(0).getName(), is(fname1));
        assertThat(list.get(1).getName(), is(fname2));
        assertThat(list.get(2).getName(), is(fname3));

        verify(channel).ls("*.xml");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReturnFilesGivenProcessedFilesExists() throws SftpException {
        List<ChannelSftp.LsEntry> lsEntries = newArrayList(entry, entry);

        when(entry.getFilename()).thenReturn(fname1).thenReturn(fname2);
        when(channel.ls("processed/*.xml")).thenReturn(new Vector(lsEntries)); //NOPMD

        List<Gaps2File> list = sftp.listProcessed();
        assertThat(list.size(), is(2));
        assertThat(list.get(0).getName(), is(fname1));
        assertThat(list.get(1).getName(), is(fname2));

        verify(channel).ls("processed/*.xml");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReturnFileGivenFailedFileExists() throws SftpException {
        List<ChannelSftp.LsEntry> lsEntries = newArrayList(entry);

        when(entry.getFilename()).thenReturn(fname1);
        when(channel.ls("failed/*.xml")).thenReturn(new Vector(lsEntries)); //NOPMD

        List<Gaps2File> list = sftp.listFailed();
        assertThat(list.size(), is(1));
        assertThat(list.get(0).getName(), is(fname1));

        verify(channel).ls("failed/*.xml");
    }

    @Test
    public void shouldReturnInputStreamGivenAFileName() throws IOException, SftpException {
        InputStream is = sftp.getInputStream("xxx");
        assertThat(IOUtils.toString(is, Charset.defaultCharset()), is("abc"));

        verify(channel).get("xxx");
    }

    @Test
    public void shouldThrowExceptionGettingIsGivenGetFails() throws SftpException {
        doThrow(new SftpCustomException("", null)).when(channel).get("xxx");
        try {
            sftp.getInputStream("xxx");
            fail();
        } catch (SftpCustomException e) {
            verify(channel).get("xxx");
        }
    }

    @Test
    public void shouldMoveAFileGivenTheDestination() throws SftpException, JSchException {
        sftp.move(true, "xxx");
        verify(channel).connect();
        verify(channel).put(SftpChannelAdapter.DUMMY, "processed/xxx");
    }

    @Test(expected = SftpCustomException.class)
    public void shouldThrowExceptionGivenMoveFails() throws SftpException {
        doThrow(new SftpCustomException("", null)).when(channel).cd("in");
        sftp.move(true, "xxx");
    }
}
