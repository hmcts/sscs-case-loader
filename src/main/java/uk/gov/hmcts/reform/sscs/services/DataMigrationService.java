package uk.gov.hmcts.reform.sscs.services;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

@Service
@Slf4j
public class DataMigrationService {

    public static final String MIGRATION_FILE_NAME = "encoded_interpreter_data.txt";

    public void process() throws IOException {
        //extract case data
        //updateCases
        String read = Files.readAllLines(Paths.get(MIGRATION_FILE_NAME)).get(0);
        String decodedString = new String(Base64.getDecoder().decode(read));
        JSONArray data = new JSONArray(decodedString);
        System.out.println(data);
    }
}
