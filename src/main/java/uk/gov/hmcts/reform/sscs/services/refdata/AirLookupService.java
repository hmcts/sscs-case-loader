package uk.gov.hmcts.reform.sscs.services.refdata;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;


/**
 * Service that ingests a spreadsheet and a csv file containing the
 * venues and regional centres for handling all post codes.
 */

@Service
public class AirLookupService {
    private static final Logger LOG = getLogger(AirLookupService.class);
    private static final int POSTCODE_COLUMN = 1;
    private static final int REGIONAL_CENTRE_COLUMN = 3;

    private Map<String, String> lookupRegionalCentreByPostCode;

    /**
     * Read in the AIRLookup RC spreadsheet and the venue id csv file.
     */
    @PostConstruct
    public void init() {
        lookupRegionalCentreByPostCode = new HashMap<>();

        try {
            ClassPathResource classPathResource = new ClassPathResource("reference-data/AIRLookup RC.xls");

            parseSpreadSheet(classPathResource);

            LOG.debug("Regional centre data has " + lookupRegionalCentreByPostCode.keySet().size() + " post codes");


            LOG.debug("Successfully loaded lookup data for postcode endpoint");
        } catch (IOException e) {
            LOG.error("Unable to read in spreadsheet with post code data: reference-data/AIRLookup RC.xls", e);
        }
    }

    /**
     * Read in spreadsheet and populate the Map with postcode.
     * @param classPathResource The file containing the spreadsheet
     * @throws IOException pass up any IO errors
     */
    private void parseSpreadSheet(ClassPathResource classPathResource) throws IOException {
        try (NPOIFSFileSystem fs = new NPOIFSFileSystem(classPathResource.getInputStream());
             HSSFWorkbook wb = new HSSFWorkbook(fs.getRoot(), true)) {

            for (Sheet sheet: wb) {
                if (sheet.getSheetName().equals("AIR")) {
                    for (Row row : sheet) {
                        Cell postcodeCell = row.getCell(POSTCODE_COLUMN);
                        Cell adminGroupCell = row.getCell(REGIONAL_CENTRE_COLUMN);
                        if (postcodeCell != null && adminGroupCell != null
                            && postcodeCell.getCellTypeEnum() == CellType.STRING
                            && adminGroupCell.getCellTypeEnum() == CellType.STRING) {
                            LOG.debug("Post code: "
                                + postcodeCell.getRichStringCellValue().getString().toLowerCase(Locale.getDefault())
                                + " Regional office: "
                                + adminGroupCell.getRichStringCellValue().getString());
                            lookupRegionalCentreByPostCode.put(
                                postcodeCell.getRichStringCellValue().getString().toLowerCase(Locale.getDefault()),
                                adminGroupCell.getRichStringCellValue().getString()
                            );
                        }
                    }
                }
            }
        }
    }

    public String lookupRegionalCentre(String postcode) {
        //full post code
        if (postcode.length() >= 5) {
            int index = postcode.length() - 3;
            //trim last 3 chars to leave the outcode
            String outcode = postcode.toLowerCase(Locale.getDefault()).substring(0, index).trim();
            return lookupRegionalCentreByPostCode.get(outcode);
        } else {
            //try it as the outcode
            return lookupRegionalCentreByPostCode.get(postcode.toLowerCase(Locale.getDefault()).trim());
        }
    }
}
