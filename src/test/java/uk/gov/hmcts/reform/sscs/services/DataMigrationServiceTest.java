package uk.gov.hmcts.reform.sscs.services;

import static uk.gov.hmcts.reform.sscs.job.DataMigrationJob.MAPPED_LANGUAGE_COLUMN;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.services.ccd.CcdCasesSender;


@ExtendWith(MockitoExtension.class)
class DataMigrationServiceTest {

    private static final String ENCODED_DATA_STRING = "W3sicmVmZXJlbmNlIjoiMTcwMzAyMTkyNDYwMDQxOCIsIiI6IiIsImV2ZW50X"
        + "2lkIjoiIHZhbGlkQXBwZWFsQ3JlYXRlZCAiLCJleGlzdGluZ19sYW5ndWFnZV92YWx1ZSI6IiAgICAgICAgICAgICAgICAgICAgICAgICAg"
        + "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICIsIm1hcHBlZF9sYW5ndWFnZV92YWx1ZSI6IkFyYWJpYyIsInN1Ym1pdHRl"
        + "ZF9sYW5ndWFnZSI6IjIwNzI0NzEwNTAiLCJpbnRlcnByZXRlciI6IiBZZXMiLCJzdGF0ZSI6IiByZWFkeVRvTGlzdCAgICAgICIsIlVtYSBD"
        + "b21tZW50cyI6Ikxhbmd1YWdlIG5vdCBjYXB0dXJlZCBjb3JyZWN0bHkiLCJjYXNlX2RhdGFfaWQiOiIxMjM0NTY3In0seyJyZWZlcmVuY2Ui"
        + "OiIxNzAzMDIxOTgxODg4NjY2IiwiIjoiIiwiZXZlbnRfaWQiOiIgdmFsaWRBcHBlYWxDcmVhdGVkICIsImV4aXN0aW5nX2xhbmd1YWdlX3Zh"
        + "bHVlIjoiICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIiwibWFwcGVkX2xh"
        + "bmd1YWdlX3ZhbHVlIjoiQmVuZ2FsaSIsInN1Ym1pdHRlZF9sYW5ndWFnZSI6IiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg"
        + "ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIiwiaW50ZXJwcmV0ZXIiOiIgWWVzIiwic3R"
        + "hdGUiOiIgaGVhcmluZyAgICAgICAgICAiLCJVbWEgQ29tbWVudHMiOiIiLCJjYXNlX2RhdGFfaWQiOiIxMjM0NTY4In1d";

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
        ReflectionTestUtils.setField(underTest, "encodedDataString", ENCODED_DATA_STRING);
        underTest.process(MAPPED_LANGUAGE_COLUMN);
    }
}
