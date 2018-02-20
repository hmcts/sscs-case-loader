package uk.gov.hmcts.reform.sscs.refdataloaders;

import com.opencsv.CSVReader;

import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GoogleMapRefDataLoader {

    private static final String CSV_FILE_PATH = "src/main/resources/reference-data/google-map-venues-addresses-url.csv";
    private Map<String, String> venueIdGoogleUrlmap = new HashMap<>();

    @PostConstruct
    private void init() {
        try (CSVReader reader = new CSVReader(new FileReader(CSV_FILE_PATH))) {

            List<String[]> linesList = reader.readAll();
            venueIdGoogleUrlmap = linesList.stream()
                .collect(
                    Collectors.toMap(
                        line -> line[0], line -> line[1],
                        (oldValue, newValue) -> newValue
                    ));
        } catch (Exception e) {
            log.error("Error occurred when loading the venueId-GoogleMapUrls reference data: " + e);
        }
    }

    public Map<String, String> getVenueIdGoogleUrlmap() {
        return venueIdGoogleUrlmap;
    }
}
