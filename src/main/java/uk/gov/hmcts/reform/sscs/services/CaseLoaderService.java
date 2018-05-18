package uk.gov.hmcts.reform.sscs.services;

import java.util.List;
import javax.xml.stream.XMLStreamException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.exceptions.TransformException;
import uk.gov.hmcts.reform.sscs.models.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.refdata.RefDataFactory;
import uk.gov.hmcts.reform.sscs.services.ccd.CcdCasesSender;
import uk.gov.hmcts.reform.sscs.services.ccd.SearchCcdService;
import uk.gov.hmcts.reform.sscs.services.gaps2.files.Gaps2File;
import uk.gov.hmcts.reform.sscs.services.idam.IdamService;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpSshService;
import uk.gov.hmcts.reform.sscs.services.xml.XmlValidator;

@Service
@Slf4j
public class CaseLoaderService {

    private final SftpSshService sftpSshService;
    private final XmlValidator xmlValidator;
    private final TransformationService transformService;
    private final SearchCcdService searchCcdService;
    private final CcdCasesSender ccdCasesSender;
    private final RefDataFactory refDataFactory;
    private final IdamService idamService;

    @Autowired
    CaseLoaderService(SftpSshService sftpSshService, XmlValidator xmlValidator, TransformationService transformService,
                      SearchCcdService ccdCaseService, CcdCasesSender ccdCasesSender,
                      RefDataFactory refDataFactory, IdamService idamService) {
        this.sftpSshService = sftpSshService;
        this.xmlValidator = xmlValidator;
        this.transformService = transformService;
        this.searchCcdService = ccdCaseService;
        this.ccdCasesSender = ccdCasesSender;
        this.refDataFactory = refDataFactory;
        this.idamService = idamService;
    }

    public void process() {
        log.debug("*** case-loader *** reading files from sFTP...");
        List<Gaps2File> files = sftpSshService.getFiles();
        log.debug("*** case-loader *** About to start processing files: {}", files);
        String serviceAuthorization = idamService.generateServiceAuthorization();
        IdamTokens idamTokens = IdamTokens.builder()
            .idamOauth2Token(idamService.getIdamOauth2Token())
            .serviceAuthorisation(serviceAuthorization)
            .serviceUserId(idamService.getServiceUserId(serviceAuthorization))
            .build();
        Gaps2File latestRef = null;
        for (Gaps2File file : files) {
            log.debug("*** case-loader *** file being processed: {}", file.getName());
            xmlValidator.validateXml(file);
            log.debug("*** case-loader *** file validated successfully: {}", file.getName());
            if (file.isDelta()) {
                if (null == latestRef) {
                    throw new TransformException(String.format("No reference data processed for this delta: %s",
                        file.getName()));
                }
                processDelta(idamTokens, file);
                sftpSshService.move(file, true);
                sftpSshService.move(latestRef, true);
            } else {
                latestRef = file;
                try {
                    refDataFactory.extract(sftpSshService.readExtractFile(file));
                } catch (XMLStreamException e) {
                    throw new TransformException("Error processing reference file", e);
                }
            }
        }
    }

    private void processDelta(IdamTokens idamTokens, Gaps2File file) {
        List<CaseData> cases = transformService.transform(sftpSshService.readExtractFile(file));
        log.debug("*** case-loader *** file transformed to {} Cases successfully", cases.size());
        for (CaseData caseData : cases) {
            if (!caseData.getAppeal().getBenefitType().getCode().equals("ERR")) {
                log.debug("*** case-loader *** searching case {} in CDD", caseData.getCaseReference());
                List<CaseDetails> casesByCaseRef = searchCcdService.findCaseByCaseRef(
                    caseData.getCaseReference(), idamTokens);
                log.debug("*** case-loader *** found cases in CCD: {}", casesByCaseRef);
                if (casesByCaseRef.isEmpty()) {
                    log.debug("*** case-loader *** sending case for creation to CCD: {}", caseData);
                    ccdCasesSender.sendCreateCcdCases(caseData, idamTokens);
                } else {
                    log.debug("*** case-loader *** sending case for update to CCD: {}", caseData);
                    ccdCasesSender.sendUpdateCcdCases(caseData, casesByCaseRef.get(0), idamTokens);
                }
            }
        }
    }

}
