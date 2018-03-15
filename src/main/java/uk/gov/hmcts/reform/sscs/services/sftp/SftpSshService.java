package uk.gov.hmcts.reform.sscs.services.sftp;

import java.io.InputStream;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.exceptions.FailedXmlFileException;
import uk.gov.hmcts.reform.sscs.services.gaps2.files.Gaps2File;

@Service
@Slf4j
public class SftpSshService {

    private final SftpChannelAdapter channelAdapter;

    @Autowired
    public SftpSshService(SftpChannelAdapter channelAdapter) {
        this.channelAdapter = channelAdapter;
    }

    @SuppressWarnings("unchecked")
    public List<Gaps2File> getFiles() {
        List<Gaps2File> failed = channelAdapter.listFailed();
        if (failed.isEmpty()) {
            List<Gaps2File> processed = channelAdapter.listProcessed();
            List<Gaps2File> incoming = channelAdapter.listIncoming();
            incoming.removeAll(processed);
            return incoming;
        }
        log.error("Erroneous file exists in SFTP incoming/failed directory");
        throw new FailedXmlFileException(failed.get(0).getName());
    }

    public void move(Gaps2File file, boolean success) {
        channelAdapter.move(success, file.getName());
    }

    public InputStream readExtractFile(Gaps2File file) {
        return channelAdapter.getInputStream(file.getName());
    }
}
