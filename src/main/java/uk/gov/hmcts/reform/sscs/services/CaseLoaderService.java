package uk.gov.hmcts.reform.sscs.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.exceptions.GapsValidationException;
import uk.gov.hmcts.reform.sscs.exceptions.TransformException;
import uk.gov.hmcts.reform.sscs.models.JsonFiles;
import uk.gov.hmcts.reform.sscs.models.XmlFiles;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.services.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.sscs.services.mapper.TransformJsonCasesToCaseData;
import uk.gov.hmcts.reform.sscs.services.mapper.TransformXmlFilesToJsonFiles;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpSshService;
import uk.gov.hmcts.reform.sscs.services.xml.XmlValidator;

@Service
@Slf4j
public class CaseLoaderService {

    private final SftpSshService sftpSshService;
    private final XmlValidator xmlValidator;
    private final TransformXmlFilesToJsonFiles transformXmlFilesToJsonFiles;
    private final TransformJsonCasesToCaseData transformJsonCasesToCaseData;
    private final CoreCaseDataService coreCaseDataService;

    @Autowired
    public CaseLoaderService(SftpSshService sftpSshService, XmlValidator xmlValidator,
                             TransformXmlFilesToJsonFiles transformXmlFilesToJsonFiles,
                             TransformJsonCasesToCaseData transformJsonCasesToCaseData,
                             CoreCaseDataService coreCaseDataService) {
        this.sftpSshService = sftpSshService;
        this.xmlValidator = xmlValidator;
        this.transformXmlFilesToJsonFiles = transformXmlFilesToJsonFiles;
        this.transformJsonCasesToCaseData = transformJsonCasesToCaseData;
        this.coreCaseDataService = coreCaseDataService;
    }

    public void process() {
        List<InputStream> inputStreamList = sftpSshService.readExtractFiles();
        log.info("*** case-loader *** Read Delta xml file from SFTP successfully");
        inputStreamList.forEach(inputStream -> {
            String deltaXmlAsString = fromInputStreamToString(inputStream);
            validateXml(deltaXmlAsString);
            log.info("*** case-loader *** Validate Delta xml file successfully");
            CaseData caseData = transformDeltaToCaseData(deltaXmlAsString);
            log.info("*** case-loader *** Transform Delta xml file into CCD Cases successfully");
            CaseDetails caseDetails = coreCaseDataService.startEventAndSaveGivenCase(caseData);
            log.info("*** case-loader *** Save CDD case into CCD successfully: {}",
                printCaseDetailsInJson(caseDetails));
        });
    }

    private String printCaseDetailsInJson(CaseDetails caseDetails) {
        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json()
            .indentOutput(true)
            .build();
        try {
            return mapper.writeValueAsString(caseDetails);
        } catch (JsonProcessingException e) {
            log.error("Fail to serialise CaseDetails", e);
        }
        return null;
    }

    private String fromInputStreamToString(InputStream inputStream) {
        try {
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            throw new TransformException("Failed to transform inputStream to String", e);
        }
    }

    private CaseData transformDeltaToCaseData(String deltaAsString) {
        CaseData caseData;
        try {
            caseData = transformDeltaToCaseDataUnHandled(deltaAsString);
        } catch (IOException e) {
            throw new TransformException("Failed to transform xml to CCD data", e);
        }
        return caseData;
    }

    private void validateXml(String deltaAsString) {
        try {
            xmlValidator.validateXml(deltaAsString, "Delta");
        } catch (SAXException | XMLStreamException | IOException e) {
            throw new GapsValidationException("Failed to validate xml", e);
        }
    }

    private CaseData transformDeltaToCaseDataUnHandled(String deltaAsString) throws IOException {
        XmlFiles xmlFiles = XmlFiles.builder().delta(deltaAsString).ref("nothing for now").build();
        JsonFiles jsonCases = transformXmlFilesToJsonFiles.transform(xmlFiles);
        return transformJsonCasesToCaseData.transform(jsonCases.getDelta().toString());
    }

}
