package uk.gov.hmcts.reform.sscs.util;

import static uk.gov.hmcts.reform.sscs.util.MigrationStringUtils.compressAndB64Encode;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;

@Slf4j
public class MigrationDataEncoderApp {

    public static final String MIGRATION_FILE = "local_migration_data.csv";
    public static final String ENCODED_STR_FILE = "ENCODED_" + MIGRATION_FILE.replace(".csv", ".txt");

    private MigrationDataEncoderApp() {
    }

    public static void main(String[] args) {
        CsvSchema bootstrap = CsvSchema.emptySchema().withHeader();
        CsvMapper csvMapper = new CsvMapper();
        File migrationFile = new File(MIGRATION_FILE);

        try (MappingIterator<Map<String, String>> mappingIterator =
                 csvMapper.readerFor(Map.class).with(bootstrap).readValues(migrationFile)) {

            log.info("Parsing migration file ({}) to generate encoded string of migration data json", MIGRATION_FILE);

            List<Map<String, String>> migrationData = mappingIterator.readAll();
            log.info("encoding data for {} cases", migrationData.size());

            JSONArray migrationDataJson = new JSONArray(migrationData);

            String encodedMigrationData = compressAndB64Encode(migrationDataJson.toString());
            Path path = Paths.get(LocalDate.now().toString().replace("-", "")
                .concat("_" + ENCODED_STR_FILE));
            Files.write(path, encodedMigrationData.getBytes());

            log.info("Generated encoded string and saved it under {}", path);

        } catch (IOException e) {
            log.error("There was a problem encoding the migration data, migration file ({}), encoded string file ({})",
                MIGRATION_FILE, ENCODED_STR_FILE);
            throw new RuntimeException(e);
        }
    }
}
