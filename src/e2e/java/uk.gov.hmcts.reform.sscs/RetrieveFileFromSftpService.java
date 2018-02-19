package uk.gov.hmcts.reform.sscs;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sscs.models.GapsInputStream;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpSshService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RetrieveFileFromSftpService {

    @Autowired
    private SftpSshService service;

    @Test
    public void givenAnSftpFile_shouldBeRetrievedAndConvertedToAnInputStream() throws Exception {

        List<GapsInputStream> result = service.readExtractFiles();

        assertThat(result, hasSize(1));
        InputStream stream;
        stream = result.get(0).getInputStream();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
            assertEquals(br.readLine(), "<?xml version=\"1.0\" standalone=\"yes\"?>");
        } finally {
            stream.close();
        }
    }
}
