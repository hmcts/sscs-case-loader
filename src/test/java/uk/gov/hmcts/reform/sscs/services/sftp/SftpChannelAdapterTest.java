package uk.gov.hmcts.reform.sscs.services.sftp;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import com.jcraft.jsch.*;
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
    ChannelSftp.LsEntry entry;

    private SftpSshProperties props = new SftpSshProperties();
    private String fname1 = "SSCS_Extract_Delta_2000-01-01-01-01-01.xml";
    private String fname2 = "SSCS_Extract_Delta_2000-01-01-01-01-02.xml";
    private String fname3 = "SSCS_Extract_Delta_2000-01-01-01-01-03.xml";
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
        stub(channel.get("in/xxx")).toReturn(new ByteArrayInputStream("abc".getBytes()));

        sftp = new SftpChannelAdapter(jsch, props);
    }

    @After
    public void tearDown() throws Exception {
        verify(jsch).addIdentity("SSCS-SFTP", "key".getBytes(),null, null);
        verify(jsch).getSession("user", "host", 123);
        verify(channel).connect();
        verify(session).setConfig("StrictHostKeyChecking", "no");
        verify(session).connect(60000);
        verify(session).openChannel("sftp");

        verifyNoMoreInteractions(jsch, session, channel);
    }

    @Test
    public void shouldReturnListOfFilesGivenPath() throws SftpException {
        List<ChannelSftp.LsEntry> lsEntries = newArrayList(entry, entry, entry);

        when(entry.getFilename()).thenReturn(fname1)
            .thenReturn(fname2)
            .thenReturn(fname3);
        when(channel.ls("in/xxx")).thenReturn(new Vector(lsEntries));

        List<Gaps2File> list = sftp.list("/xxx");
        assertThat(list.size(), is(3));
        assertThat(list.get(0).getName(), is(fname1));
        assertThat(list.get(1).getName(), is(fname2));
        assertThat(list.get(2).getName(), is(fname3));

        verify(channel).ls("in/xxx");
    }

    @Test
    public void shouldReturnInputStreamGivenAFileName() throws IOException, SftpException {
        InputStream is = sftp.getInputStream("xxx");
        assertThat(IOUtils.toString(is, Charset.defaultCharset()), is("abc"));

        verify(channel).get("in/xxx");
    }

    @Test
    public void shouldMoveAFileGivenTheDestination() throws SftpException, JSchException {
        sftp.move("xxx");
        verify(channel).connect();
        verify(channel).put("", "in/xxx");
    }
}
