package uk.gov.hmcts.reform.sscs.services.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
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

    @Autowired
    public SftpChannelAdapter(JSch jsch, SftpSshProperties sftpSshProperties) {
        this.jsch = jsch;
        this.sftpSshProperties = sftpSshProperties;
    }

    @PostConstruct
    public void init() {
        ChannelSftp sftp = getSftpChannel();
        for (String dirName : new String[]{PROCESSED_DIR, FAILED_DIR}) {
            try {
                sftp.stat(dirName);
            } catch (SftpException e) {
                try {
                    sftp.mkdir(dirName);
                } catch (SftpException e1) {
                    throw new SftpCustomException("Error creating directory", dirName, e);
                }
            }
        }
    }

    private ChannelSftp getSftpChannel() {
        try {
            jsch.addIdentity("SSCS-SFTP", sftpSshProperties.getKeyLocation().getBytes(),
                null, null);
        } catch (JSchException e) {
            throw new SftpCustomException("AddIdentity error", e);
        }
        Session sesConnection;
        try {
            sesConnection = jsch.getSession(
                sftpSshProperties.getUsername(),
                sftpSshProperties.getHost(),
                sftpSshProperties.getPort());
            sesConnection.setConfig("StrictHostKeyChecking", "no");
            sesConnection.connect(60000);
        } catch (JSchException e) {
            throw new SftpCustomException("Session connection error", e);
        }
        ChannelSftp channel;
        try {
            channel = (ChannelSftp) sesConnection.openChannel("sftp");
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
        ChannelSftp channel = getSftpChannel();
        List<ChannelSftp.LsEntry> ls;
        try {
            ls = channel.ls(String.format("%s*.xml", path));
        } catch (SftpException e) {
            throw new SftpCustomException("Failed reading incoming directory", e);
        }
        return ls.stream().map(e -> new Gaps2File(e.getFilename())).collect(Collectors.toList());
    }

    public InputStream getInputStream(String fileName) {
        ChannelSftp channel = getSftpChannel();
        try {
            return channel.get(fileName);
        } catch (SftpException e) {
            throw new SftpCustomException("Failed reading file stream", fileName, e);
        }
    }

    public void move(boolean success, String fileName) {
        ChannelSftp sftpChannel = getSftpChannel();
        try {
            sftpChannel.put(DUMMY, String.format("%s%s", success ? PROCESSED_DIR : FAILED_DIR, fileName));
        } catch (SftpException e) {
            throw new SftpCustomException("Failed moving file", fileName, e);
        }
    }

}
