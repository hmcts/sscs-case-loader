package uk.gov.hmcts.reform.sscs.services;

import java.util.List;
import javax.xml.stream.XMLStreamException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.exceptions.TransformException;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.refdata.RefDataFactory;
import uk.gov.hmcts.reform.sscs.services.ccd.CcdCasesSender;
import uk.gov.hmcts.reform.sscs.services.ccd.SearchCoreCaseDataService;
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
    private final SearchCoreCaseDataService ccdCaseService;
    private final CcdCasesSender ccdCasesSender;
    private final RefDataFactory refDataFactory;
    private final IdamService idamService;

    @Autowired
    public CaseLoaderService(SftpSshService sftpSshService,
                             XmlValidator xmlValidator,
                             TransformationService transformService,
                             SearchCoreCaseDataService ccdCaseService,
                             CcdCasesSender ccdCasesSender,
                             RefDataFactory refDataFactory, IdamService idamService) {
        this.sftpSshService = sftpSshService;
        this.xmlValidator = xmlValidator;
        this.transformService = transformService;
        this.ccdCaseService = ccdCaseService;
        this.ccdCasesSender = ccdCasesSender;
        this.refDataFactory = refDataFactory;
        this.idamService = idamService;
    }

    public void process() {
        log.debug("*** case-loader *** reading files from sFTP...");
        List<Gaps2File> files = sftpSshService.getFiles();
        log.debug("*** case-loader *** About to start processing files: {}", files);
        String idamOauth2Token = idamService.getIdamOauth2Token();
        for (Gaps2File file : files) {
            log.debug("*** case-loader *** file being processed: {}", file.getName());
            xmlValidator.validateXml(file);
            log.debug("*** case-loader *** file validated successfully: {}", file.getName());
            if (file.isDelta()) {
                List<CaseData> cases = transformService.transform(sftpSshService.readExtractFile(file));
                log.debug("*** case-loader *** file transformed to Cases successfully");
                for (CaseData caseData : cases) {
                    log.debug("*** case-loader *** searching case {} in CDD", caseData.getCaseReference());
                    List<CaseDetails> casesByCaseRef = ccdCaseService.findCaseByCaseRef(caseData.getCaseReference(),
                        idamOauth2Token);
                    log.debug("*** case-loader *** found cases in CCD: {}", casesByCaseRef);
                    if (casesByCaseRef.isEmpty()) {
                        log.debug("*** case-loader *** sending case for creation to CCD: {}", caseData);
                        ccdCasesSender.sendCreateCcdCases(caseData, idamOauth2Token);
                    } else {
                        log.debug("*** case-loader *** sending case for update to CCD: {}", caseData);
                        ccdCasesSender.sendUpdateCcdCases(caseData, casesByCaseRef.get(0), idamOauth2Token);
                    }
                }
                sftpSshService.move(file, true);
            } else {
                try {
                    refDataFactory.extract(sftpSshService.readExtractFile(file));
                    sftpSshService.move(file, true);
                } catch (XMLStreamException e) {
                    log.error("Error processing reference file", e);
                    throw new TransformException("Error processing reference file", e);
                }
            }
        }
    }

}
