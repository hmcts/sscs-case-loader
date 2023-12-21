package uk.gov.hmcts.reform.sscs.util;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.sscs.ccd.domain.State.DORMANT_APPEAL_STATE;
import static uk.gov.hmcts.reform.sscs.ccd.domain.State.VOID_STATE;
import static uk.gov.hmcts.reform.sscs.ccd.domain.YesNo.YES;

@Slf4j
public class MigrationDataEncoderApp {

    public static final String MIGRATION_FILE = "example_mapped_language_data.csv";
    public static final String ENCODED_STRING_FILE = "encoded_migration_data.txt";

    private static final String INTERPRETER_COLUMN = "interpreter";
    private static final String STATE_COLUMN = "state";

    public static void main(String[] args) {
        CsvSchema bootstrap = CsvSchema.emptySchema().withHeader();
        CsvMapper csvMapper = new CsvMapper();
        File migrationFile = new File(MIGRATION_FILE);

        try (MappingIterator<Map<String, String>> mappingIterator =
                 csvMapper.readerFor(Map.class).with(bootstrap).readValues(migrationFile)) {

            List<Map<String, String>> migrationData = mappingIterator.readAll();
            migrationData.removeIf(row -> !row.get(INTERPRETER_COLUMN).trim().equals(YES.toString()));
            migrationData.removeIf(row -> row.get(STATE_COLUMN).trim().equals(VOID_STATE.toString())
                || row.get(STATE_COLUMN).trim().equals(DORMANT_APPEAL_STATE.toString()));
            JSONArray jsonObject = new JSONArray(migrationData);
            String encodedMigrationData = Base64.getEncoder().encodeToString(jsonObject.toString().getBytes());

            Path path = Paths.get(ENCODED_STRING_FILE);
            Files.write(path, encodedMigrationData.getBytes());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
