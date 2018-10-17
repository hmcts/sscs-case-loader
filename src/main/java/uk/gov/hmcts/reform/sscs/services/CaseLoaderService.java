package uk.gov.hmcts.reform.sscs.services;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.ccd.service.CcdService;
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
    private final CcdService ccdService;

    @Autowired
    CaseLoaderService(SftpSshService sftpSshService, XmlValidator xmlValidator, TransformationService transformService,
                      CcdCasesSender ccdCasesSender, RefDataFactory refDataFactory, IdamService idamService,
                      CcdService ccdService) {
        this.sftpSshService = sftpSshService;
        this.xmlValidator = xmlValidator;
        this.transformService = transformService;
        this.ccdCasesSender = ccdCasesSender;
        this.refDataFactory = refDataFactory;
        this.idamService = idamService;
        this.ccdService = ccdService;
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
        log.debug("*** case-loader *** file transformed to {} Cases successfully", cases.size());
        for (SscsCaseData caseData : cases) {
            if (!caseData.getAppeal().getBenefitType().getCode().equals("ERR")) {

                List<SscsCaseDetails> sscsCcdCases = Collections.emptyList();

                if (StringUtils.isNotBlank(caseData.getCaseReference())) {
                    log.info("*** case-loader *** searching case reference {} in CDD", caseData.getCaseReference());
                    sscsCcdCases = ccdService
                        .findCaseBy(ImmutableMap.of("case.caseReference", caseData.getCaseReference()), idamTokens);
                }

                if (sscsCcdCases.isEmpty()
                    && StringUtils.isNotBlank(caseData.getCcdCaseId())) {
                    log.info("*** case-loader *** searching case ccd id {} in CDD", caseData.getCcdCaseId());
                    SscsCaseDetails sscsCaseDetails = ccdService
                        .getByCaseId(Long.parseLong(caseData.getCcdCaseId()), idamTokens);

                    if (null != sscsCaseDetails) {
                        sscsCcdCases = Collections.singletonList(sscsCaseDetails);
                    }
                }

                log.debug("*** case-loader *** found cases in CCD: {}", sscsCcdCases);
                if (sscsCcdCases.isEmpty()) {
                    log.debug("*** case-loader *** sending case for creation to CCD: {}", caseData);
                    ccdCasesSender.sendCreateCcdCases(caseData, idamTokens);
                } else {
                    log.debug("*** case-loader *** sending case for update to CCD: {}", caseData);
                    ccdCasesSender.sendUpdateCcdCases(caseData, sscsCcdCases.get(0), idamTokens);
                }
            }
        }
    }

}
