package uk.gov.hmcts.reform.sscs.olde2e;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.services.gaps2.files.Gaps2File;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpSshService;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("development")
public class RetrieveFileFromSftpService {

    @Autowired
    private SftpSshService service;

    @Test
    public void shouldBeRetrievedAndConvertedToAnInputStreamGivenAnSftpFile() throws IOException {

        List<Gaps2File> files = service.getFiles();

        assertThat(files, hasSize(2));
        InputStream stream;
        stream = service.readExtractFile(files.get(0));
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
            assertEquals(br.readLine(), "<?xml version=\"1.0\" standalone=\"yes\"?>");
        } finally {
            stream.close();
        }
    }
}
