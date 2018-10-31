package uk.gov.hmcts.reform.sscs.services;

import java.util.List;
import javax.xml.stream.XMLStreamException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.SearchCcdCaseService;
import uk.gov.hmcts.reform.sscs.exceptions.TransformException;
import uk.gov.hmcts.reform.sscs.idam.IdamService;
import uk.gov.hmcts.reform.sscs.idam.IdamTokens;
import uk.gov.hmcts.reform.sscs.refdata.RefDataFactory;
import uk.gov.hmcts.reform.sscs.services.ccd.CcdCasesSender;
import uk.gov.hmcts.reform.sscs.services.gaps2.files.Gaps2File;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpSshService;
import uk.gov.hmcts.reform.sscs.services.xml.XmlValidator;

@Service
@Slf4j
public class CaseLoaderService {

    private final SftpSshService sftpSshService;
    private final XmlValidator xmlValidator;
    private final TransformationService transformService;
    private final CcdCasesSender ccdCasesSender;
    private final RefDataFactory refDataFactory;
    private final IdamService idamService;
    private final SearchCcdCaseService searchCcdCaseService;

    @Autowired
    CaseLoaderService(SftpSshService sftpSshService, XmlValidator xmlValidator, TransformationService transformService,
                      CcdCasesSender ccdCasesSender, RefDataFactory refDataFactory, IdamService idamService,
                      SearchCcdCaseService searchCcdCaseService) {
        this.sftpSshService = sftpSshService;
        this.xmlValidator = xmlValidator;
        this.transformService = transformService;
        this.ccdCasesSender = ccdCasesSender;
        this.refDataFactory = refDataFactory;
        this.idamService = idamService;
        this.searchCcdCaseService = searchCcdCaseService;
    }

    public void process() {
        log.debug("*** case-loader *** reading files from sFTP...");
        List<Gaps2File> files = sftpSshService.getFiles();
        log.debug("*** case-loader *** About to start processing files: {}", files);
        String oauth2Token = idamService.getIdamOauth2Token();
        IdamTokens idamTokens = IdamTokens.builder()
            .idamOauth2Token(oauth2Token)
            .serviceAuthorization(idamService.generateServiceAuthorization())
            .userId(idamService.getUserId(oauth2Token))
            .build();
        Gaps2File latestRef = null;
        for (Gaps2File file : files) {
            log.info("*** case-loader *** file being processed: {}", file.getName());
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
        List<SscsCaseData> cases = transformService.transform(sftpSshService.readExtractFile(file));
        log.info("*** case-loader *** file transformed to {} Cases successfully", cases.size());
        for (SscsCaseData caseData : cases) {
            if (!caseData.getAppeal().getBenefitType().getCode().equals("ERR")) {
                SscsCaseDetails sscsCaseDetails;
                try {
                    sscsCaseDetails = searchCcdCaseService.findCaseByCaseRefOrCaseId(caseData, idamTokens);
                } catch (NumberFormatException e) {
                    log.info("*** case-loader *** case with SC {} and ccdID {} could not be searched for,"
                        + " skipping case...",
                        caseData.getCaseReference(), caseData.getCcdCaseId());
                    continue;
                }
                
                if (null == sscsCaseDetails) {
                    log.info("*** case-loader *** case with SC {} and ccdID {} does not exist, it will be created...",
                        caseData.getCaseReference(), caseData.getCcdCaseId());
                    ccdCasesSender.sendCreateCcdCases(caseData, idamTokens);
                } else {
                    log.info("*** case-loader *** case with SC {} and ccdID {} exists, it will be updated...",
                        caseData.getCaseReference(), caseData.getCcdCaseId());
                    ccdCasesSender.sendUpdateCcdCases(caseData, sscsCaseDetails, idamTokens);
                }
            }
        }
    }

}
