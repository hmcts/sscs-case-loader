package uk.gov.hmcts.reform.sscs.services.refdata;

import static com.google.common.collect.Maps.newHashMap;
import static org.slf4j.LoggerFactory.getLogger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.exceptions.RegionalProcessingCenterServiceException;
import uk.gov.hmcts.reform.sscs.models.refdata.RegionalProcessingCenter;



@Service
public class RegionalProcessingCenterService {
    private static final Logger LOG = getLogger(RegionalProcessingCenterService.class);

    public static final String RPC_DATA_JSON = "reference-data/rpc-data.json";
    private static final String CSV_FILE_PATH = "reference-data/sscs-venues.csv";
    public static final char SEPARATOR_CHAR = '/';
    public static final String SSCS_BIRMINGHAM = "SSCS Birmingham";

    private Map<String, RegionalProcessingCenter>  regionalProcessingCenterMap  = newHashMap();
    private final Map<String, String> sccodeRegionalProcessingCentermap = newHashMap();


    @PostConstruct
    public void init() {
        loadSccodeRpcMetadata();
        populateRpcMetadata();
    }

    private void loadSccodeRpcMetadata() {
        ClassPathResource classPathResource = new ClassPathResource(CSV_FILE_PATH);
        try (CSVReader reader = new CSVReader(new InputStreamReader(classPathResource.getInputStream()))) {

            List<String[]> linesList = reader.readAll();

            linesList.forEach(line ->
                    sccodeRegionalProcessingCentermap.put(line[1], line[2])
            );
        } catch (IOException e) {
            LOG.error("Error occurred while loading the sscs venues reference data file: " + CSV_FILE_PATH,
                new RegionalProcessingCenterServiceException(e));
        }
    }

    private void populateRpcMetadata() {
        ClassPathResource classPathResource = new ClassPathResource(RPC_DATA_JSON);
        try (InputStream inputStream  = classPathResource.getInputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            regionalProcessingCenterMap =
                    mapper.readValue(inputStream, new TypeReference<Map<String,RegionalProcessingCenter>>(){});

        } catch (IOException e) {
            LOG.error("Error while reading RegionalProcessingCenter from " + RPC_DATA_JSON,
                new RegionalProcessingCenterServiceException(e));
        }
    }


    public RegionalProcessingCenter getByScReferenceCode(String referenceNumber) {
        String[] splitReferenceNumber = StringUtils.split(referenceNumber, SEPARATOR_CHAR);
        String regionalProcessingCenter = sccodeRegionalProcessingCentermap.get(splitReferenceNumber[0]);

        if (null != regionalProcessingCenter) {
            return regionalProcessingCenterMap.get(regionalProcessingCenter);
        } else {
            return regionalProcessingCenterMap.get(SSCS_BIRMINGHAM);
        }
    }

    public Map<String, RegionalProcessingCenter> getRegionalProcessingCenterMap() {
        return regionalProcessingCenterMap;
    }

    public Map<String, String> getSccodeRegionalProcessingCentermap() {
        return sccodeRegionalProcessingCentermap;
    }

}
