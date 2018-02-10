package uk.gov.hmcts.reform.sscs.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.exceptions.TransformException;
import uk.gov.hmcts.reform.sscs.models.GapsInputStream;
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

        List<GapsInputStream> inputStreamList = sftpSshService.readExtractFiles();
        log.info("*** case-loader *** Read xml files from SFTP successfully");
        inputStreamList.forEach(gapsInputStream -> {
            String xmlAsString = fromInputStreamToString(gapsInputStream.getInputStream());
            String type = gapsInputStream.getIsDelta() ? "Delta" : "Reference";
            validateXml(xmlAsString, type);
            log.info("*** case-loader *** Validate " + type + " xml file successfully");
            List<CaseData> caseDataList = transformStringToCaseData(xmlAsString);
            log.info("*** case-loader *** Transform " + type + " xml file into CCD Cases successfully");
            caseDataList.forEach(caseData -> {
                CaseDetails caseDetails = coreCaseDataService.startEventAndSaveGivenCase(caseData);
                log.info("*** case-loader *** Save case details into CCD successfully: {}",
                    printCaseDetailsInJson(caseDetails));
            });
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

    private List<CaseData> transformStringToCaseData(String input) {
        List<CaseData> caseDataList;
        try {
            caseDataList = transformInputToCaseDataUnHandled(input);
        } catch (IOException e) {
            throw new TransformException("Failed to transform xml to CCD data", e);
        }
        return caseDataList;
    }

    private void validateXml(String inputAsString, String type) {
        try {
            xmlValidator.validateXml(inputAsString, type);
        } catch (Exception e) {
            log.error("Something wrong when validating the xml files", e);
        }
    }

    private List<CaseData> transformInputToCaseDataUnHandled(String input) throws IOException {
        JSONObject jsonCases = transformXmlFilesToJsonFiles.transform(input);
        return transformJsonCasesToCaseData.transform(jsonCases.toString());
    }

}
