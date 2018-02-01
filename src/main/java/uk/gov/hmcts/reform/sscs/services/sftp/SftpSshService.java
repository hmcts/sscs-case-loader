package uk.gov.hmcts.reform.sscs.services.sftp;

import com.jcraft.jsch.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.config.properties.SftpSshProperties;

@Service
@Slf4j
public class SftpSshService {

    private final JSch jschSshChannel;
    private final SftpSshProperties sftpSshProperties;

    @Autowired
    public SftpSshService(JSch jschSshChannel, SftpSshProperties sftpSshProperties) {
        this.jschSshChannel = jschSshChannel;
        this.sftpSshProperties = sftpSshProperties;
    }

    public List<InputStream> readExtractFiles() {
        try {
            return getFilesAsInputStreams(connect());
        } catch (JSchException | SftpException e) {
            log.error(e.getMessage());
        }
        return Collections.emptyList();
    }

    public Session connect() throws JSchException {
        jschSshChannel.addIdentity(sftpSshProperties.getKeyLocation());

        Session sesConnection = jschSshChannel.getSession(
            sftpSshProperties.getUsername(),
            sftpSshProperties.getHost(),
            sftpSshProperties.getPort());
        sesConnection.setConfig("StrictHostKeyChecking", "no");
        sesConnection.connect(60000);

        return sesConnection;
    }

    public List<InputStream> getFilesAsInputStreams(Session sesConnection) throws JSchException, SftpException {
        Channel channel = sesConnection.openChannel("sftp");
        channel.connect();
        ChannelSftp channelSftp = (ChannelSftp) channel;

        List fileList = channelSftp.ls(sftpSshProperties.getInputDirectory() + "/*.xml");

        List<InputStream> inputStreams = new ArrayList<>();

        for (Object file : fileList) {
            inputStreams.add(channelSftp.get(sftpSshProperties.getInputDirectory() + "/"
                + ((ChannelSftp.LsEntry) file).getFilename()));
        }
        return inputStreams;
    }

}
