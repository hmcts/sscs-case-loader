package uk.gov.hmcts.reform.sscs.services.sftp;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class SftpSshService {

    @Value("${sftp.host}")
    private String host;

    @Value("${sftp.port}")
    private int port;

    @Value("${sftp.user}")
    private String username;

    @Value("${sftp.input.dir}")
    private String sftpInputDirectory;

    @Value("${sftp.key.location}")
    private String sftpKeyLocation;

    @Autowired
    private JSch jschSshChannel;

    public List<InputStream> readExtractFiles()  {
        try {
            return getFilesAsInputStreams(connect());
        } catch (JSchException | SftpException e) {
            log.error(e.getMessage());
        }
        return Collections.emptyList();
    }

    public Session connect() throws JSchException {
        jschSshChannel.addIdentity(sftpKeyLocation);

        Session sesConnection = jschSshChannel.getSession(username, host, port);
        sesConnection.setConfig("StrictHostKeyChecking", "no");
        sesConnection.connect(60000);

        return sesConnection;
    }

    public List<InputStream> getFilesAsInputStreams(Session sesConnection) throws JSchException, SftpException {
        Channel channel = sesConnection.openChannel("sftp");
        channel.connect();
        ChannelSftp channelSftp = (ChannelSftp) channel;

        List fileList = channelSftp.ls(sftpInputDirectory + "/*.xml");

        List<InputStream> inputStreams = new ArrayList<>();

        for (Object file : fileList) {
            inputStreams.add(channelSftp.get(sftpInputDirectory + "/" + ((ChannelSftp.LsEntry)file).getFilename()));
        }
        return inputStreams;
    }

}
