package uk.gov.hmcts.reform.sscs.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.exceptions.ApplicationErrorException;
import uk.gov.hmcts.reform.sscs.exceptions.TransformException;
import uk.gov.hmcts.reform.sscs.models.GapsInputStream;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.services.ccd.CreateCoreCaseDataService;
import uk.gov.hmcts.reform.sscs.services.ccd.UpdateCoreCaseDataService;
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
    private final CreateCoreCaseDataService createCoreCaseDataService;
    private final UpdateCoreCaseDataService updateCoreCaseDataService;

    @Autowired
    public CaseLoaderService(SftpSshService sftpSshService, XmlValidator xmlValidator,
                             TransformXmlFilesToJsonFiles transformXmlFilesToJsonFiles,
                             TransformJsonCasesToCaseData transformJsonCasesToCaseData,
                             CreateCoreCaseDataService createCoreCaseDataService,
                             UpdateCoreCaseDataService updateCoreCaseDataService) {
        this.sftpSshService = sftpSshService;
        this.xmlValidator = xmlValidator;
        this.transformXmlFilesToJsonFiles = transformXmlFilesToJsonFiles;
        this.transformJsonCasesToCaseData = transformJsonCasesToCaseData;
        this.createCoreCaseDataService = createCoreCaseDataService;
        this.updateCoreCaseDataService = updateCoreCaseDataService;
    }

    public void process() {
        log.info("*** case-loader *** Reading xml files from SFTP...");
        List<GapsInputStream> inputStreamList = sftpSshService.readExtractFiles();
        log.info("*** case-loader *** Read xml files from SFTP successfully");
        List<CaseData> caseDataList = new ArrayList<>();
        List<CaseData> updateCaseList = new ArrayList<>();
        for (GapsInputStream gapsInputStream : inputStreamList) {
            String xmlAsString = fromInputStreamToString(gapsInputStream.getInputStream());
            String type = gapsInputStream.getIsDelta() ? "Delta" : "Reference";
            xmlValidator.validateXml(xmlAsString, type);
            log.info("*** case-loader *** Validate " + type + " xml file successfully");
            if ("Delta".equals(type)) {
                JSONObject jsonCases = transformXmlFilesToJsonFiles.transform(xmlAsString);
                log.info("*** case-loader *** Transform XML to JSON successfully");
                caseDataList = transformJsonCasesToCaseData.transformCreateCases(jsonCases.toString());
                log.info("*** case-loader *** Transform CreateCases to JSON successfully");
                updateCaseList = transformJsonCasesToCaseData.transformUpdateCases(jsonCases.toString());
                log.info("*** case-loader *** Transform UpdateCases to JSON successfully");
            }
        }
        sendCreateCcdCases(caseDataList);
        sendUpdateCcdCases(updateCaseList);
    }

    private void sendCreateCcdCases(List<CaseData> caseDataList) {
        caseDataList.forEach(caseData -> {
            log.info("*** case-loader *** About to save case into CCD: {}", printCaseDetailsInJson(caseData));
            CaseDetails caseDetails = createCoreCaseDataService.createCcdCase(caseData);
            log.info("*** case-loader *** Save case into CCD successfully: {}", printCaseDetailsInJson(caseDetails));
        });
    }

    private void sendUpdateCcdCases(List<CaseData> caseDataList) {
        caseDataList.forEach(caseData -> {
            log.info("*** case-loader *** About to update case into CCD: {}", printCaseDetailsInJson(caseData));
            // TODO: 25/02/2018 call find and update ccd api
            log.info("*** case-loader *** Update case into CCD successfully:");
        });
    }

    private String printCaseDetailsInJson(Object object) {

        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json()
            .indentOutput(true)
            .build();
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new ApplicationErrorException("Oops...something went wrong...", e);
        }
    }

    private String fromInputStreamToString(InputStream inputStream) {
        try {
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            throw new TransformException("Oops...something went wrong...", e);
        }
    }

}
