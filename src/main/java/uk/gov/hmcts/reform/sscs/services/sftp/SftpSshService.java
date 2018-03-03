package uk.gov.hmcts.reform.sscs.services.sftp;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.config.properties.SftpSshProperties;
import uk.gov.hmcts.reform.sscs.exceptions.SftpCustomException;
import uk.gov.hmcts.reform.sscs.models.GapsInputStream;

@Service
@Slf4j
public class SftpSshService {

    private final JSch jschSshChannel;
    private final SftpSshProperties sftpSshProperties;
    private static final String SSCS_SFTP = "SSCS-SFTP";
    private static final String DELTA_FILE_START = "SSCS_Extract_Delta";
    private static final String REFERENCE_FILE_START = "SSCS_Extract_Reference";

    @Autowired
    public SftpSshService(JSch jschSshChannel, SftpSshProperties sftpSshProperties) {
        this.jschSshChannel = jschSshChannel;
        this.sftpSshProperties = sftpSshProperties;
    }

    public List<GapsInputStream> readExtractFiles() {
        return getFilesAsInputStreams(connect());
    }

    private Session connect() {
        try {
            jschSshChannel.addIdentity(SSCS_SFTP, sftpSshProperties.getKeyLocation().getBytes(),
                null, null);

            Session sesConnection = jschSshChannel.getSession(
                sftpSshProperties.getUsername(),
                sftpSshProperties.getHost(),
                sftpSshProperties.getPort());
            sesConnection.setConfig("StrictHostKeyChecking", "no");
            sesConnection.connect(60000);

            return sesConnection;
        } catch (JSchException e) {
            throw new SftpCustomException("Oops...something went wrong...", e);
        }
    }

    private List<GapsInputStream> getFilesAsInputStreams(Session sesConnection) {
        try {
            Channel channel = sesConnection.openChannel("sftp");
            channel.connect();
            ChannelSftp channelSftp = (ChannelSftp) channel;

            List fileList = channelSftp.ls(sftpSshProperties.getInputDirectory() + "/*.xml");

            List<String> listOfGaps2Files = getOrderedListOfGaps2FilesByDateTime(fileList);

            List<GapsInputStream> inputStreams = new ArrayList<>();

            for (String fileName : listOfGaps2Files) {
                log.info("Sftp file: {}", fileName);
                InputStream stream = channelSftp.get(sftpSshProperties.getInputDirectory() + "/"
                    + fileName);

                inputStreams.add(GapsInputStream.builder()
                    .isDelta(isFileType(fileName, DELTA_FILE_START))
                    .isReference(isFileType(fileName, REFERENCE_FILE_START))
                    .inputStream(stream)
                    .build());
            }
            return inputStreams;
        } catch (JSchException | SftpException e) {
            throw new SftpCustomException("Oops...something went wrong...", e);
        }
    }

    private List<String> getOrderedListOfGaps2FilesByDateTime(List fileList) {
        List<String> fileNameList = new ArrayList<>();

        for (Object lsEntry: fileList) {
            fileNameList.add(((ChannelSftp.LsEntry) lsEntry).getFilename());
        }

        return Gaps2FileUtils.getOrderByDateAndTime(fileNameList);
    }

    private Boolean isFileType(String fileName, String startPath) {
        return fileName.startsWith(startPath);
    }
}
