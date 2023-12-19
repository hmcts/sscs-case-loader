package uk.gov.hmcts.reform.sscs.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.services.ccd.CcdCasesSender;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@ExtendWith(MockitoExtension.class)
class DataMigrationServiceTest {

    @Mock
    private CcdCasesSender ccdCasesSender;
    @Mock
    private IdamService idamService;

    DataMigrationService underTest;

    @BeforeEach
    void setUp() {
        underTest = new DataMigrationService(ccdCasesSender, idamService);
    }

    @Test
    void process() throws IOException {
        byte[] encodedBytes =  Files.readAllBytes(Paths.get("src/test/resources/example_encoded_migration_data.txt"));
        ReflectionTestUtils.setField(underTest, "encodedDataString", new String(encodedBytes));
        underTest.process();
    }
}
