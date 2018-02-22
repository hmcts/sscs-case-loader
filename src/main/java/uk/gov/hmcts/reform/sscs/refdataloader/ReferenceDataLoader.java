package uk.gov.hmcts.reform.sscs.refdataloader;

import com.opencsv.CSVReader;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.sscs.models.refdata.VenueDetails;

@Service
@Slf4j
public class ReferenceDataLoader {

    private static final String CSV_FILE_PATH = "src/main/resources/reference-data/sscs-venues.csv";
    private final List<VenueDetails> venueDetailsList = new ArrayList<>();

    @PostConstruct
    private void init() {
        try (CSVReader reader = new CSVReader(new FileReader(CSV_FILE_PATH))) {

            List<String[]> linesList = reader.readAll();
            linesList.forEach(line ->
                venueDetailsList.add(
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
                        .build())
            );
        } catch (IOException e) {
            log.error("Error occurred while loading the sscs venues reference data file: " + CSV_FILE_PATH + e);
        }
    }

    public List<VenueDetails> getVenueDetailsList() {
        return venueDetailsList;
    }
}
