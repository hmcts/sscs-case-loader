package uk.gov.hmcts.reform.sscs.services.gaps2;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.sscs.services.gaps2.files.Gaps2File;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpSshService;

public class Gaps2FileLoader {

    private final LocalDate ignoreDate;

    private SftpSshService sftp;

    @Autowired
    public Gaps2FileLoader(@Value("${sscs.case.loader.ignoreCasesBeforeDate}") String ignoreCasesBeforeDateProperty,
                           SftpSshService sftp) {
        this.ignoreDate = LocalDate.parse(ignoreCasesBeforeDateProperty).minusDays(1);
        this.sftp = sftp;
    }

    public Gaps2File getNextFile() {
        List<Gaps2File> fileList = sftp.getFiles();
        Optional<Gaps2File> first = fileList.stream()
            .filter(f -> f.getDate().toLocalDate().isAfter(ignoreDate))
            .sorted()
            .findFirst();
        return first.isPresent() ? first.get() : null;
    }

    public void processed(Gaps2File file) {
        sftp.move(file, true);
    }

    public void failed(Gaps2File file) {
        sftp.move(file, false);
    }
}
