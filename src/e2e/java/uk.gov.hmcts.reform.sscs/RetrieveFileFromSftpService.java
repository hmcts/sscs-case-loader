package uk.gov.hmcts.reform.sscs;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpSshService;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;

/**
 * Class for testing purpose.
 * When running the SFTP dockerized env we can use the method here to confirm
 * that we can read files from SFTP successfully.
 */
//TODO: Move this class to our End to End tests project
@RunWith(SpringRunner.class)
@SpringBootTest
public class RetrieveFileFromSftpService {

    @Autowired
    private SftpSshService service;

    @Test
    @Ignore
    public void givenAnSftpFile_shouldBeRetrievedAndConvertedToAnInputStream() throws Exception {

        List<InputStream> result = service.readExtractFiles();

        assertThat(result, hasSize(1));
        InputStream stream;
        stream = result.get(0);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
            assertEquals(br.readLine(), "<?xml version=\"1.0\" standalone=\"yes\"?>");
        } finally {
            stream.close();
        }
    }
}
