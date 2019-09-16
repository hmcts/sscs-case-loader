package uk.gov.hmcts.reform.sscs.refdata;

import static com.google.common.collect.Maps.newHashMap;

import com.opencsv.CSVReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.models.refdata.VenueDetails;

@Service
@Slf4j
public class VenueDataLoader {

    private static final String CSV_FILE_PATH = "reference-data/sscs-venues.csv";
    private final Map<String, VenueDetails> venueDetailsMap = newHashMap();

    @PostConstruct
    private void init() {
        InputStream is = getClass().getClassLoader().getResourceAsStream(CSV_FILE_PATH);
        try (CSVReader reader = new CSVReader(new InputStreamReader(is))) {

            List<String[]> linesList = reader.readAll();
            linesList.forEach(line ->
                venueDetailsMap.put(line[0],
                    VenueDetails.builder()
                        .venueId(line[0])
                        .threeDigitReference(line[1])
                        .regionalProcessingCentre(line[2])
                        .venName(line[3])
                        .venAddressLine1(line[4])
                        .venAddressLine2(line[5])
                        .venAddressTown(line[6])
                        .venAddressCounty(line[7])
                        .venAddressPostcode(line[8])
                        .venAddressTelNo(line[9])
                        .districtId(line[10])
                        .url(line[11])
                        .active(line[12])
                        .build())
            );
        } catch (IOException e) {
            log.error("Error occurred while loading the sscs venues reference data file: " + CSV_FILE_PATH + e);
        }
    }

    public Map<String, VenueDetails> getVenueDetailsMap() {
        return venueDetailsMap;
    }
}
