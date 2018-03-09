package uk.gov.hmcts.reform.sscs.services.sftp;

import static com.google.common.collect.Lists.newArrayList;

import com.jcraft.jsch.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.config.properties.SftpSshProperties;
import uk.gov.hmcts.reform.sscs.exceptions.SftpCustomException;
import uk.gov.hmcts.reform.sscs.models.GapsInputStream;
import uk.gov.hmcts.reform.sscs.services.gaps2.files.Gaps2File;

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

    public List<GapsInputStream> readExtractFiles() {
        ChannelSftp channel = getSftpChannel();

        List<Gaps2File> listOfGaps2Files = getFiles();

        List<GapsInputStream> inputStreams = new ArrayList<>();

        for (Gaps2File file : listOfGaps2Files) {
            log.info("Sftp file: {}", file.getName());
            InputStream stream;
            try {
                stream = channel.get(sftpSshProperties.getInputDirectory() + "/"
                    + file.getName());
            } catch (SftpException e) {
                throw new SftpCustomException(
                    String.format("SFTP Failed to get stream for file: %s", file.getName()), e);
            }

            inputStreams.add(GapsInputStream.builder()
                .isDelta(file.isDelta())
                .isReference(!file.isDelta())
                .inputStream(stream)
                .build());
        }
        return inputStreams;
    }

    public List<Gaps2File> getFiles() {
        ChannelSftp channel = getSftpChannel();

        List<ChannelSftp.LsEntry> ls;
        try {
            ls = channel.ls(sftpSshProperties.getInputDirectory() + "/*.xml");
        } catch (SftpException e) {
            throw new SftpCustomException("SFTP Failed reading incoming directory", e);
        }
        List<Gaps2File> fileList = newArrayList();
        for (ChannelSftp.LsEntry entry : ls) {
            fileList.add(newFile(entry.getFilename()));
        }
        return fileList;
    }

    private Gaps2File newFile(String filename) {
        return new Gaps2File(filename);
    }

    public void move(Gaps2File file, boolean success) {
        // TODO: move loaded file to processed or failed directory
    }

    private ChannelSftp getSftpChannel() {
        try {
            jschSshChannel.addIdentity("SSCS-SFTP", sftpSshProperties.getKeyLocation().getBytes(),
                null, null);

            Session sesConnection = jschSshChannel.getSession(
                sftpSshProperties.getUsername(),
                sftpSshProperties.getHost(),
                sftpSshProperties.getPort());
            sesConnection.setConfig("StrictHostKeyChecking", "no");
            sesConnection.connect(60000);

            Channel channel = sesConnection.openChannel("sftp");
            channel.connect();
            return (ChannelSftp) channel;
        } catch (JSchException e) {
            throw new SftpCustomException("Oops...something went wrong...", e);
        }
    }
}
