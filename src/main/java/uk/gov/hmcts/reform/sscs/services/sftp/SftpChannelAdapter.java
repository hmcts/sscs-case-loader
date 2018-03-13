package uk.gov.hmcts.reform.sscs.services.sftp;

import com.jcraft.jsch.*;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sscs.config.properties.SftpSshProperties;
import uk.gov.hmcts.reform.sscs.exceptions.SftpCustomException;
import uk.gov.hmcts.reform.sscs.services.gaps2.files.Gaps2File;

@Component
public class SftpChannelAdapter {

    private final JSch jschSshChannel;
    private final SftpSshProperties sftpSshProperties;

    @Autowired
    public SftpChannelAdapter(JSch jschSshChannel, SftpSshProperties sftpSshProperties) {
        this.jschSshChannel = jschSshChannel;
        this.sftpSshProperties = sftpSshProperties;
    }

    private ChannelSftp getSftpChannel() {
        try {
            jschSshChannel.addIdentity("SSCS-SFTP", sftpSshProperties.getKeyLocation().getBytes(),
                null, null);
        } catch (JSchException e) {
            throw new SftpCustomException("AddIdentity error", e);
        }
        Session sesConnection;
        try {
            sesConnection = jschSshChannel.getSession(
                sftpSshProperties.getUsername(),
                sftpSshProperties.getHost(),
                sftpSshProperties.getPort());
        } catch (JSchException e) {
            throw new SftpCustomException("Get session error", e);
        }
        sesConnection.setConfig("StrictHostKeyChecking", "no");
        try {
            sesConnection.connect(60000);
        } catch (JSchException e) {
            throw new SftpCustomException("Session connection error", e);
        }
        Channel channel;
        try {
            channel = sesConnection.openChannel("sftp");
        } catch (JSchException e) {
            throw new SftpCustomException("Open channel error", e);
        }
        try {
            channel.connect();
        } catch (JSchException e) {
            throw new SftpCustomException("Connect to channel error", e);
        }
        return (ChannelSftp) channel;
    }

    @SuppressWarnings("unchecked")
    public List<Gaps2File> list(String path) {
        ChannelSftp channel = getSftpChannel();
        List<ChannelSftp.LsEntry> ls;
        try {
            ls = channel.ls(sftpSshProperties.getInputDirectory() + path);
        } catch (SftpException e) {
            throw new SftpCustomException("Failed reading incoming directory", e);
        }
        return ls.stream().map(e -> new Gaps2File(e.getFilename())).collect(Collectors.toList());
    }

    public InputStream getInputStream(String fileName) {
        ChannelSftp channel = getSftpChannel();
        try {
            return channel.get(sftpSshProperties.getInputDirectory() + "/" + fileName);
        } catch (SftpException e) {
            throw new SftpCustomException("Failed reading file stream", fileName, e);
        }
    }

    public void move(String fileName) {
        try {
            getSftpChannel().put("", String.format("%s/%s", sftpSshProperties.getInputDirectory(), fileName));
        } catch (SftpException e) {
            throw new SftpCustomException("Failed moving file", fileName, e);
        }
    }
}
