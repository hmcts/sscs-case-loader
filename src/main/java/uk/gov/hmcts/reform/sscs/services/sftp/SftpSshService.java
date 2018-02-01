package uk.gov.hmcts.reform.sscs.services.sftp;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class SftpSshService {
    private static final Logger LOG = getLogger(SftpSshService.class);

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
        } catch (JSchException e) {
            LOG.error(e.getMessage());
        } catch (SftpException e) {
            LOG.error(e.getMessage());
        }
        return null;
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

        Vector fileList = channelSftp.ls(sftpInputDirectory + "/*.xml");

        List<InputStream> inputStreams = new ArrayList<>();

        for (Object file : fileList) {
            inputStreams.add(channelSftp.get(sftpInputDirectory + "/" + ((ChannelSftp.LsEntry)file).getFilename()));
        }
        return inputStreams;
    }

}
