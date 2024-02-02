package uk.gov.hmcts.reform.sscs.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MigrationStringUtils {

    private MigrationStringUtils() {
    }

    public static String compressAndB64Encode(String text) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(outputStream)) {
            deflaterOutputStream.write(text.getBytes());
        }
        return new String(Base64.getEncoder().encode(outputStream.toByteArray()));
    }

    public static String decompressAndB64Decode(String b64Compressed) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (OutputStream inflaterOutputStream = new InflaterOutputStream(outputStream)) {
            inflaterOutputStream.write(Base64.getDecoder().decode(b64Compressed));
        }
        return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
    }
}
