package uk.gov.hmcts.reform.sscs.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Doc;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Documents;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.Evidence;
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

        for (GapsInputStream gapsInputStream : inputStreamList) {
            String xmlAsString = fromInputStreamToString(gapsInputStream.getInputStream());
            String type = gapsInputStream.getIsDelta() ? "Delta" : "Reference";
            xmlValidator.validateXml(xmlAsString, type);
            log.info("*** case-loader *** Validate " + type + " xml file successfully");
            if ("Delta".equals(type)) {
                JSONObject jsonCases = transformXmlFilesToJsonFiles.transform(xmlAsString);
                log.info("*** case-loader *** Transform XML to JSON successfully");
                List<CaseData> casesToCreate = transformJsonCasesToCaseData.transformCreateCases(jsonCases.toString());
                log.info("*** case-loader *** Transform json to cases to create successfully");
                List<CaseData> casesToUpdate = transformJsonCasesToCaseData.transformUpdateCases(jsonCases.toString());
                log.info("*** case-loader *** Transform json to cases to update successfully");
                sendCreateCcdCases(casesToCreate);
                sendUpdateCcdCases(casesToUpdate);
            }
        }
    }

    private void sendCreateCcdCases(List<CaseData> caseDataList) {
        caseDataList.forEach(caseData -> {
            log.info("*** case-loader *** About to save case into CCD: {}", printCaseDetailsInJson(caseData));
            List<CaseDetails> cases = searchCoreCaseDataService.findCaseByCaseRef(caseData.getCaseReference());
            if (cases.isEmpty()) {
                CaseDetails caseDetails = createCoreCaseDataService.createCcdCase(caseData);
                log.info("*** case-loader *** Save case into CCD successfully: {}",
                    printCaseDetailsInJson(caseDetails));
            }
        });
    }

    private void sendUpdateCcdCases(List<CaseData> caseDataList) {
        for (CaseData caseData : caseDataList) {
            log.info("*** case-loader *** Found potential case to update in Delta: {}", caseData.getCaseReference());
            List<CaseDetails> cases = searchCoreCaseDataService.findCaseByCaseRef(caseData.getCaseReference());
            log.info("*** case-loader *** Cases found with caseRef: {} in CCD: {}", caseData.getCaseReference(),
                printCaseDetailsInJson(cases));

            if (!cases.isEmpty()) {
                String latestEventType = caseData.getLatestEventType();
                if (latestEventType != null) {
                    CaseDetails existingCcdCase = cases.get(0);
                    checkNewEvidenceReceived(caseData, existingCcdCase);
                    ifThereIsEventChangesThenUpdateCase(caseData, existingCcdCase);
                }
            }
        }
    }

    private void ifThereIsEventChangesThenUpdateCase(CaseData caseData, CaseDetails existingCcdCase) {
        //        List<Events> newEvents = caseData.getEvents();
        //        System.out.println("newEvents: " + newEvents);
        //        System.out.println("newEvents.size" + newEvents.size());
        //        List<Events> existingEvents = (List<Events>) existingCcdCase.getData().get("events");
        //        System.out.println("existingEvents: " + existingEvents);
        //        System.out.println("existingEvents.size:" + existingEvents.size());

        CaseDetails caseDetails;
        log.info("*** case-loader *** About to update case into CCD: {}", printCaseDetailsInJson(caseData));
        caseDetails = updateCoreCaseDataService.updateCase(caseData, existingCcdCase.getId(),
            caseData.getLatestEventType());
        log.info("*** case-loader *** Update case in CCD successfully: {}", caseDetails);
    }

    public void checkNewEvidenceReceived(CaseData caseData, CaseDetails existingCase) {
        Evidence newEvidence = caseData.getEvidence();
        Evidence existingEvidence = buildExistingEvidence(existingCase);

        if (newEvidence != null && existingEvidence != null && !existingEvidence.equals(newEvidence)) {
            CaseDetails caseDetails = updateCoreCaseDataService
                .updateCase(caseData, existingCase.getId(), "evidenceReceived");
            log.info("*** case-loader *** New evidence received event: {}", caseDetails);
        }
    }

    @SuppressWarnings("unchecked")
    private Evidence buildExistingEvidence(CaseDetails existingCase) {
        List<HashMap<String, Object>> documents = (List<HashMap<String, Object>>) (
            (HashMap) existingCase.getData().get("evidence")).get("documents");

        List<Documents> documentList = new ArrayList<>();
        for (HashMap doc : documents) {
            Map<String, Object> docValue = (HashMap<String, Object>) doc.get("value");

            documentList.add(Documents.builder().value(
                Doc.builder()
                    .dateReceived((String) docValue.get("dateReceived"))
                    .description((String) docValue.get("description"))
                    .build())
                .build());
        }

        return Evidence.builder().documents(documentList).build();
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
