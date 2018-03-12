package uk.gov.hmcts.reform.sscs.services.sftp;

import java.io.InputStream;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
        return channelAdapter.list("/*.xml");
    }

    public void move(Gaps2File file, boolean success) {
        String fileName = success
            ? String.format("/processed/%s", file.getName())
            : String.format("/failed/%s", file.getName());
        channelAdapter.move(fileName);
    }

    public InputStream readExtractFile(Gaps2File file) {
        return channelAdapter.getInputStream(file.getName());
    }
}
