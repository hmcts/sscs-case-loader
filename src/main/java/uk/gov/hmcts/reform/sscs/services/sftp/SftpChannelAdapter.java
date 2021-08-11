package uk.gov.hmcts.reform.sscs.services.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.config.properties.SftpSshProperties;
import uk.gov.hmcts.reform.sscs.exceptions.SftpCustomException;
import uk.gov.hmcts.reform.sscs.services.gaps2.files.Gaps2File;

@Slf4j
@Component
public class SftpChannelAdapter {

    private static final String PROCESSED_DIR = "processed/";
    private static final String FAILED_DIR = "failed/";

    public static final ByteArrayInputStream DUMMY = new ByteArrayInputStream("".getBytes());

    private final JSch jsch;
    private final SftpSshProperties sftpSshProperties;

    private ThreadLocal<Session> threadSession;
    private ThreadLocal<Map<Integer, ChannelSftp>> threadChannels;

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
    public ChannelSftp openConnectedChannel(int channelId) throws JSchException {

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

    private Session openSession() {
        Session session = threadSession.get();
        if (session == null) {
            session = statelessConnect();
            threadSession.set(session);
        }
        return session;
    }

    private Session statelessConnect() {
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
        return list("");
    }

    public List<Gaps2File> listFailed() {
        return list(FAILED_DIR);
    }

    public List<Gaps2File> listProcessed() {
        return list(PROCESSED_DIR);
    }

    @SuppressWarnings("unchecked")
    private List<Gaps2File> list(String path) {
        ChannelSftp channel = null;
        List<ChannelSftp.LsEntry> ls;
        try {
            channel = openConnectedChannel();
            ls = channel.ls(String.format("%s*.xml", path));
            List<Gaps2File> files = ls.stream().map(e -> new Gaps2File(e.getFilename(), e.getAttrs().getSize()))
                .sorted()
                .collect(Collectors.toList());
            log.info("No of files {} in directory {}", files.size(), path);
            if (PROCESSED_DIR.equals(path) && files.isEmpty()) {
                throw new SftpCustomException(path + " directory should not be empty");
            }
            return files;
        } catch (SftpException e) {
            throw new SftpCustomException("Failed reading incoming directory", e);
        } catch (Exception e) {
            throw new SftpCustomException(String.format("Exception on %s directory", path), e);
        } finally {
            close();
        }

    }

    public InputStream getInputStream(String fileName) {
        ChannelSftp sftp = null;
        try {
            sftp = openConnectedChannel();
            InputStream in = sftp.get(fileName);
            byte[] targetArray = IOUtils.toByteArray(in);
            return new ByteArrayInputStream(targetArray);
        } catch (SftpException | IOException e) {
            throw new SftpCustomException("Failed reading file stream", fileName, e);
        } finally {
            close();
        }
    }

    public void move(boolean success, String fileName) {
        ChannelSftp sftp = null;
        try {
            sftp = openConnectedChannel();
            sftp.put(DUMMY, String.format("%s%s", success ? PROCESSED_DIR : FAILED_DIR, fileName));
        } catch (SftpException e) {
            throw new SftpCustomException("Failed moving file", fileName, e);
        } finally {
            close();
        }
    }

    public void close() {
        Session session = threadSession.get();
        if (session != null) {
            session.disconnect();
            threadSession.remove();
        }
        Map<Integer, ChannelSftp> channels = threadChannels.get();
        if (channels != null) {
            channels.forEach((key, channel) -> {
                if (channel != null) {
                    channel.disconnect();
                }
            });
            channels.clear();
            threadChannels.remove();
        }
    }
}
