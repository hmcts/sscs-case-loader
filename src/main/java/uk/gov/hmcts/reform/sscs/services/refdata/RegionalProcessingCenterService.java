package uk.gov.hmcts.reform.sscs.services.refdata;

import static com.google.common.collect.Maps.newHashMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.exceptions.RegionalProcessingCenterServiceException;
import uk.gov.hmcts.reform.sscs.models.refdata.RegionalProcessingCenter;

@Service
@Slf4j
public class RegionalProcessingCenterService {

    private static final String RPC_DATA_JSON = "reference-data/rpc-data.json";
    private static final String CSV_FILE_PATH = "reference-data/sscs-venues.csv";
    private static final char SEPARATOR_CHAR = '/';
    private static final String SSCS_BIRMINGHAM = "SSCS Birmingham";

    private Map<String, RegionalProcessingCenter> regionalProcessingCenterMap = newHashMap();
    private final Map<String, String> scCodeRegionalProcessingCenterMap = newHashMap();
    private final Map<String, String> venueIdToRegionalProcessingCentre = newHashMap();

    private final AirLookupService airLookupService;

    @Autowired
    RegionalProcessingCenterService(AirLookupService airLookupService) {
        this.airLookupService = airLookupService;
    }


    @PostConstruct
    void init() {
        loadScCodeRpcMetadata();
        populateRpcMetadata();
    }

    private void loadScCodeRpcMetadata() {
        ClassPathResource classPathResource = new ClassPathResource(CSV_FILE_PATH);
        try (CSVReader reader = new CSVReader(new InputStreamReader(classPathResource.getInputStream()))) {

            List<String[]> linesList = reader.readAll();

            linesList.forEach(line -> {
                scCodeRegionalProcessingCenterMap.put(line[1], line[2]);
                venueIdToRegionalProcessingCentre.put(line[0], line[2]);
            });
        } catch (IOException e) {
            throw new RegionalProcessingCenterServiceException(
                "Error occurred while loading the sscs venues reference data file: " + CSV_FILE_PATH, e);
        }
    }

    private void populateRpcMetadata() {
        ClassPathResource classPathResource = new ClassPathResource(RPC_DATA_JSON);
        try (InputStream inputStream = classPathResource.getInputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            regionalProcessingCenterMap =
                mapper.readValue(inputStream, new TypeReference<Map<String, RegionalProcessingCenter>>() {
                });

        } catch (IOException e) {
            throw new RegionalProcessingCenterServiceException(
                "Error while reading RegionalProcessingCenter from " + RPC_DATA_JSON, e);
        }
    }

    public RegionalProcessingCenter getByScReferenceCode(String referenceNumber) {
        if (StringUtils.isBlank(referenceNumber)) {
            return regionalProcessingCenterMap.get(SSCS_BIRMINGHAM);
        }

        String[] splitReferenceNumber = StringUtils.split(referenceNumber, SEPARATOR_CHAR);
        String regionalProcessingCenter = scCodeRegionalProcessingCenterMap.get(splitReferenceNumber[0]);

        if (null != regionalProcessingCenter) {
            if (regionalProcessingCenterMap.get(regionalProcessingCenter) == null) {
                log.error("*** case-loader *** Venue could not be mapped to a valid RPC - {} for the SC number - {} "
                    + "that SSCS knows about", regionalProcessingCenter, referenceNumber);
            } else {
                return regionalProcessingCenterMap.get(regionalProcessingCenter);
            }
        }
        return regionalProcessingCenterMap.get(SSCS_BIRMINGHAM);
    }

    public RegionalProcessingCenter getByVenueId(String venueId) {
        String rpc = venueIdToRegionalProcessingCentre.get(venueId);
        return regionalProcessingCenterMap.get(rpc);
    }

    public RegionalProcessingCenter getByPostcode(String postcode) {
        String regionalProcessingCentreName = airLookupService.lookupRegionalCentre(postcode);
        return regionalProcessingCenterMap.get("SSCS " + regionalProcessingCentreName);
    }

    Map<String, RegionalProcessingCenter> getRegionalProcessingCenterMap() {
        return regionalProcessingCenterMap;
    }

    Map<String, String> getScCodeRegionalProcessingCenterMap() {
        return scCodeRegionalProcessingCenterMap;
    }
}
