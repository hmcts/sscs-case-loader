package uk.gov.hmcts.reform.sscs.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

@ExtendWith(MockitoExtension.class)
class DataMigrationServiceTest {

    DataMigrationService underTest;

    @BeforeEach
    void setUp() {
        underTest = new DataMigrationService();
    }

    @Test
    void process() throws IOException {
        underTest.process();
    }
}
