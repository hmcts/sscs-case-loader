package uk.gov.hmcts.reform.sscs.services.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.config.properties.SftpSshProperties;
import uk.gov.hmcts.reform.sscs.exceptions.SftpCustomException;
import uk.gov.hmcts.reform.sscs.services.gaps2.files.Gaps2File;

@Component
public class SftpChannelAdapter {

    private static final String PROCESSED_DIR = "processed/";
    private static final String FAILED_DIR = "failed/";

    public static final ByteArrayInputStream DUMMY = new ByteArrayInputStream("".getBytes());

    private final JSch jsch;
    private final SftpSshProperties sftpSshProperties;

    protected ThreadLocal<Session> threadSession;
    protected ThreadLocal<Map<Integer, ChannelSftp>> threadChannels;

    // Define reusable channels
    private static final int CHANNEL_1 = 0;
    private static final int CHANNEL_IN  = 2;

    @Autowired
    public SftpChannelAdapter(JSch jsch, SftpSshProperties sftpSshProperties) {
        this.jsch = jsch;
        this.sftpSshProperties = sftpSshProperties;
        this.threadSession = new ThreadLocal<>();
        this.threadChannels = new ThreadLocal<>();
    }

    private void initializeJch() {
        try {
            jsch.addIdentity("SSCS-SFTP", sftpSshProperties.getKeyLocation().getBytes(),
                    null, null);
        } catch (JSchException e) {
            throw new SftpCustomException("AddIdentity error", e);
        }
    }

    public void init() {
        ChannelSftp sftp = openConnectedChannel();
        for (String dirName : new String[]{PROCESSED_DIR, FAILED_DIR}) {
            try {
                sftp.stat(dirName);
            } catch (SftpException e) {
                try {
                    sftp.mkdir(dirName);
                } catch (SftpException e1) {
                    throw new SftpCustomException("Error creating directory", dirName, e); //NOPMD
                }
            }
        }
    }

    /**
     * Open and connect a reusable SFTP channel.
     *
     * @param channelId is the ID of the reusable channel to open.
     * @return a connected reusable channel.
     */
    protected ChannelSftp openConnectedChannel(int channelId) throws JSchException {

        Session session = openSession();
        Map<Integer, ChannelSftp> channels = threadChannels.get();
        if (channels == null) {
            channels = new HashMap<>();
        }
        ChannelSftp channel = channels.get(channelId);
        if (channel == null || channel.isClosed()) {
            channel = (ChannelSftp) session.openChannel("sftp");
        }
        if (!channel.isConnected()) {
            channel.connect();
        }
        channels.put(channelId, channel);
        threadChannels.set(channels);
        return channel;
    }

    /**
     * Open and Connect a new SFTP channel.
     *
     * @return a new channel.
     */
    public ChannelSftp openConnectedChannel() {
        ChannelSftp channel;
        try {
            Session session = openSession();
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
        } catch (JSchException e) {
            throw new SftpCustomException("Connect to channel error", e);
        }

        try {
            channel.cd(sftpSshProperties.getInputDirectory());
        } catch (SftpException e) {
            throw new SftpCustomException("Failed to change to remote directory", e);
        }
        return channel;
    }

    protected Session openSession() {
        Session session = threadSession.get();
        if (session == null) {
            session = statelessConnect();
            threadSession.set(session);
        }
        return session;
    }

    protected Session statelessConnect() {
        Session session = null;
        try {
            initializeJch();
            session = jsch.getSession(
                    sftpSshProperties.getUsername(),
                    sftpSshProperties.getHost(),
                    sftpSshProperties.getPort());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(60000);
            return session;
        } catch (JSchException e) {
            throw new SftpCustomException("Session connection error", e);
        }
    }

    public List<Gaps2File> listIncoming() {
        return list(true,"");
    }

    public List<Gaps2File> listFailed() {
        return list(true, FAILED_DIR);
    }

    public List<Gaps2File> listProcessed() {
        return list(true, PROCESSED_DIR);
    }

    @SuppressWarnings("unchecked")
    private List<Gaps2File> list(boolean closeSession, String path) {
        ChannelSftp channel = null;
        List<ChannelSftp.LsEntry> ls;
        try {
            channel = (closeSession) ? openConnectedChannel() : openConnectedChannel(CHANNEL_1);
            ls = channel.ls(String.format("%s*.xml", path));
            return ls.stream().map(e -> new Gaps2File(e.getFilename(), e.getAttrs().getSize()))
                    .sorted()
                    .collect(Collectors.toList());
        } catch (JSchException | SftpException e) {
            throw new SftpCustomException("Failed reading incoming directory", e);
        } catch (Exception e) {
            return Lists.emptyList();
        } finally {
            if (closeSession) {
                close();
            }
        }

    }

    public InputStream getInputStream(String fileName) {
        return getInputStream(fileName, true);
    }

    private InputStream getInputStream(String fileName, boolean closeSession) {
        ChannelSftp sftp = null;
        try {
            sftp = (closeSession) ? openConnectedChannel() : openConnectedChannel(CHANNEL_IN);
            return sftp.get(fileName);
        } catch (JSchException | SftpException e) {
            throw new SftpCustomException("Failed reading file stream", fileName, e);
        }
    }

    public void move(boolean success, String fileName) {
        move(success, fileName, true);
    }

    private void move(boolean success, String fileName, boolean closeSession) {
        ChannelSftp sftp = null;
        try {
            sftp = (closeSession) ? openConnectedChannel() : openConnectedChannel(CHANNEL_1);
            sftp.put(DUMMY, String.format("%s%s", success ? PROCESSED_DIR : FAILED_DIR, fileName));
        } catch (JSchException | SftpException e) {
            throw new SftpCustomException("Failed moving file", fileName, e);
        } finally {
            if (closeSession) {
                close();
            }
        }
    }

    private void close() {
        Session session = threadSession.get();
        if (session != null) {
            session.disconnect();
            threadSession.remove();
        }
        Map<Integer, ChannelSftp> channels = threadChannels.get();
        if (channels != null) {
            Iterator<Map.Entry<Integer, ChannelSftp>> itr = channels.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry<Integer, ChannelSftp> entry = itr.next();
                ChannelSftp channel = entry.getValue();
                if (channel != null) {
                    channel.disconnect();
                }
            }
            channels.clear();
            threadChannels.remove();
        }
    }
}
