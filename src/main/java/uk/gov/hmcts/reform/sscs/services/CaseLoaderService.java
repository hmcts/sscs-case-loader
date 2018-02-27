package uk.gov.hmcts.reform.sscs.services;

import static uk.gov.hmcts.reform.sscs.models.GapsEvent.APPEAL_RECEIVED;
import static uk.gov.hmcts.reform.sscs.models.GapsEvent.RESPONSE_RECEIVED;

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
import uk.gov.hmcts.reform.sscs.services.ccd.SearchCoreCaseDataService;
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
    private final SearchCoreCaseDataService searchCoreCaseDataService;
    private final UpdateCoreCaseDataService updateCoreCaseDataService;

    @Autowired
    public CaseLoaderService(SftpSshService sftpSshService, XmlValidator xmlValidator,
                             TransformXmlFilesToJsonFiles transformXmlFilesToJsonFiles,
                             TransformJsonCasesToCaseData transformJsonCasesToCaseData,
                             CreateCoreCaseDataService createCoreCaseDataService,
                             SearchCoreCaseDataService searchCoreCaseDataService,
                             UpdateCoreCaseDataService updateCoreCaseDataService) {
        this.sftpSshService = sftpSshService;
        this.xmlValidator = xmlValidator;
        this.transformXmlFilesToJsonFiles = transformXmlFilesToJsonFiles;
        this.transformJsonCasesToCaseData = transformJsonCasesToCaseData;
        this.createCoreCaseDataService = createCoreCaseDataService;
        this.searchCoreCaseDataService = searchCoreCaseDataService;
        this.updateCoreCaseDataService = updateCoreCaseDataService;
    }

    public void process() {
        log.info("*** case-loader *** Reading xml files from SFTP...");
        List<GapsInputStream> inputStreamList = sftpSshService.readExtractFiles();
        log.info("*** case-loader *** Read xml files from SFTP successfully");
        List<CaseData> appealReceivedCases = new ArrayList<>();
        List<CaseData> responseReceivedCases = new ArrayList<>();
        for (GapsInputStream gapsInputStream : inputStreamList) {
            String xmlAsString = fromInputStreamToString(gapsInputStream.getInputStream());
            String type = gapsInputStream.getIsDelta() ? "Delta" : "Reference";
            xmlValidator.validateXml(xmlAsString, type);
            log.info("*** case-loader *** Validate " + type + " xml file successfully");
            if ("Delta".equals(type)) {
                JSONObject jsonCases = transformXmlFilesToJsonFiles.transform(xmlAsString);
                log.info("*** case-loader *** Transform XML to JSON successfully");
                appealReceivedCases = transformJsonCasesToCaseData.transformCasesOfGivenStatusIntoCaseData(
                    jsonCases.toString(), APPEAL_RECEIVED.getStatus());
                log.info("*** case-loader *** Transform Appeal Received cases to CaseData successfully");
                responseReceivedCases = transformJsonCasesToCaseData.transformCasesOfGivenStatusIntoCaseData(
                    jsonCases.toString(), RESPONSE_RECEIVED.getStatus());
                log.info("*** case-loader *** Transform Response Received cases to CaseData successfully");
            }
        }
        sendCreateCcdCases(appealReceivedCases);
        sendUpdateCcdCases(responseReceivedCases);
    }

    private void sendCreateCcdCases(List<CaseData> caseDataList) {
        caseDataList.forEach(caseData -> {
            log.info("*** case-loader *** About to save case into CCD: {}", printCaseDetailsInJson(caseData));
            CaseDetails caseDetails = createCoreCaseDataService.createCcdCase(caseData);
            log.info("*** case-loader *** Save case into CCD successfully: {}", printCaseDetailsInJson(caseDetails));
        });
    }

    private void sendUpdateCcdCases(List<CaseData> caseDataList) {
        CaseDetails caseDetails;
        for (CaseData caseData : caseDataList) {
            log.info("*** case-loader *** About to update case into CCD: {}", printCaseDetailsInJson(caseData));
            List<CaseDetails> cases = searchCoreCaseDataService.findCaseByCaseRef(caseData.getCaseReference());
            log.info("*** case-loader *** Found cases with caseRef: {} in CCD: {}", caseData.getCaseReference(),
                printCaseDetailsInJson(cases));
            if (!cases.isEmpty()) {
                caseDetails = updateCoreCaseDataService.updateCase(caseData, cases.get(0).getId(),
                    "responseReceived");
                log.info("*** case-loader *** Update case into CCD successfully: {}", caseDetails);
            } else {
                caseDetails = createCoreCaseDataService.createCcdCase(caseData);
                log.info("*** case-loader *** Save case into CCD successfully: {}",
                    printCaseDetailsInJson(caseDetails));
            }

        }
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
