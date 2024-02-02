package uk.gov.hmcts.reform.sscs.util;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.sscs.util.MigrationStringUtils.compressAndB64Encode;
import static uk.gov.hmcts.reform.sscs.util.MigrationStringUtils.decompressAndB64Decode;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class MigrationStringUtilsTest {

    private static final String UNENCODED_STRING = "reference ,mapped_language_value, 1706484890513584, urdu"
        + "1706484909301633, arabic, 1706485016654517, albanian, 1706484909461544, 1706485035250891, urdu";

    private static final String ENCODED_STRING = "eJxFjFEKhDAMBa/SA/Qj2SS1PY1EjSLUIoXu+S2ou7/z5k211aqV2Zw/9DxtGbOWrelm"
        + "41dzM+9wgMCRYwJBksjetbq0hyZIBBiIvNOq0z6/vnQahAWHvuRJy67l1+ovDijMf5vkIxAT3vUL9qooXQ==";

    @Test
    void shouldCompressAndB64Encode() throws IOException {

        final String encodedString = compressAndB64Encode(UNENCODED_STRING);

        assertEquals(encodedString, ENCODED_STRING);
    }

    @Test
    void shouldDecompressAndB64Decode() throws IOException {

        final String decoded = decompressAndB64Decode(ENCODED_STRING);

        assertEquals(decoded, UNENCODED_STRING);
    }
}
