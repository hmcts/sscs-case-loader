package uk.gov.hmcts.reform.sscs.util;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

public class CreatePanelJsonFromCsv {
    private static final Logger logger = LoggerFactory.getLogger(CreatePanelJsonFromCsv.class);

    String delimiter = ",";
    String lineData;
//    csv file should have five columns: ccd_key, Session Category 1, Other Session Category, Panel Requirements 1, Panel Requirements Other
//    do a replace all for ',' and '& ' and replace with empty string (command + shift + H)
//    replace any empty cells with two apostrophes ('')
//    use online csv converter to convert xls to csv
    File csvFile = new File("johTierAutomateAttempt_2.csv");
    static int benefitIssueCodeIndex = 0;
    static int category1Index = 1;
    static int category2Index = 2;
    static int panel1ColumnIndex = 3;
    static int panel2ColumnIndex = 4;

    public void convertCsvToJson() {
        try (BufferedReader read = new BufferedReader(new FileReader(csvFile))) {
            JSONArray arrayOfObjects = new JSONArray();

            while ((lineData = read.readLine()) != null) {
                logger.debug("Reading line: {}", lineData);
                String[] data = lineData.split(delimiter);
                if (data.length >= 4) {
                    JSONObject object = getJsonObjectPanel(data,category1Index,panel1ColumnIndex);
                    if (object != null) {
                        arrayOfObjects.put(object);
                    }

                    if(!data[panel2ColumnIndex].contains("''")){
                        JSONObject object2 = getJsonObjectPanel(data,category2Index,panel2ColumnIndex);
                        if (object2 != null) {
                            arrayOfObjects.put(object2);
                        }
                    }

                    logger.debug("Added JSON object: {}", object);
                } else {
                    logger.warn("Skipping line due to insufficient columns: {}", lineData);
                }
            }

            Path path = Paths.get(LocalDate.now().toString().replace("-", "")
                .concat("_" + "johTierJsonMapping.json"));

            Files.write(path, arrayOfObjects.toString(4).getBytes()); // Indent with 4 spaces
            logger.debug("Written JSON array to file: {}", arrayOfObjects);
        } catch (IOException e) {
            logger.error("Error occurred while converting CSV to JSON", e);
        }
    }

    private static JSONObject getJsonObjectPanel(String[] data, int categoryIndex, int panelColumnIndex) {
        JSONObject object = new JSONObject();

        if (!data[panelColumnIndex].contains("''")){
            object.put("benefitIssueCode", data[benefitIssueCodeIndex]);
            // category is only one digit or NA (some comments contain numbers)
            if(trimSuperfluousInfo(data[categoryIndex]).isEmpty()) {
                object.put("category", "NA");
            }
            else {
                object.put("category", trimSuperfluousInfo(data[categoryIndex]).substring(0, 1));
            }

//            add fqpm key
            if (data[categoryIndex].contains("FQPM") && panelColumnIndex == panel2ColumnIndex) {
                object.put("fqpm", "true");
            }

            String[] johTier = data[panelColumnIndex].split(" ");
            JSONArray johTierArray = new JSONArray();
            for (String panelMember : johTier) {

//                add specialism number
                if (panelMember.contains("specialism")) {
                    if (panelColumnIndex == panel2ColumnIndex) {
                        object.put("specialism", "2");
                    }
                    else {
                        object.put("specialism", "1");
                    }
                }

//                trim panel member of specialism information
                String trimmedPanelMember = trimSuperfluousInfo(panelMember);
                if(!trimSuperfluousInfo(panelMember).isEmpty()) {
//                    correct typo (85 should be 58)
                    if (trimmedPanelMember.equals("85")){
                        trimmedPanelMember = "58";
                    }
                    johTierArray.put(trimmedPanelMember);
                }
            }
            object.put("johTier", johTierArray);

            return object;
        }
        else {
            return null;
        }

    }

//    to get rid of addition information (e.g. "+ specialism")
    private static String trimSuperfluousInfo(String data) {
        return data.replaceAll("\\D", "");
    }

    public static void main(String[] args) {
        CreatePanelJsonFromCsv converter = new CreatePanelJsonFromCsv();
        converter.convertCsvToJson();
    }
}
