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

@Slf4j
public class MigrationDataEncoderApp {

    public static final String MIGRATION_FILE_NAME = "mapped_interpreter_language.csv";

    public static void main(String[] args) {
        CsvSchema bootstrap = CsvSchema.emptySchema().withHeader();
        CsvMapper csvMapper = new CsvMapper();
        File migrationFile = new File(MIGRATION_FILE_NAME);

        try (MappingIterator<Map<String, String>> mappingIterator =
                 csvMapper.readerFor(Map.class).with(bootstrap).readValues(migrationFile)) {

            List<Map<String, String>> migrationData = mappingIterator.readAll();
            migrationData.removeIf(row -> !row.get("interpreter").trim().equals("Yes"));
            JSONArray jsonObject = new JSONArray(migrationData);
            String encodedMigrationData = Base64.getEncoder().encodeToString(jsonObject.toString().getBytes());

            Path path = Paths.get("encoded_interpreter_data.txt");
            Files.write(path, encodedMigrationData.getBytes());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
