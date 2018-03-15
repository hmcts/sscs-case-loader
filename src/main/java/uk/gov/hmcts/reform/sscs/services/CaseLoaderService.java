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

    @Autowired
    public CaseLoaderService(SftpSshService sftpSshService,
                             XmlValidator xmlValidator,
                             TransformationService transformService,
                             SearchCoreCaseDataService ccdCaseService,
                             CcdCasesSender ccdCasesSender,
                             RefDataFactory refDataFactory) {
        this.sftpSshService = sftpSshService;
        this.xmlValidator = xmlValidator;
        this.transformService = transformService;
        this.ccdCaseService = ccdCaseService;
        this.ccdCasesSender = ccdCasesSender;
        this.refDataFactory = refDataFactory;
    }

    public void process() {
        List<Gaps2File> files = sftpSshService.getFiles();

        for (Gaps2File file : files) {
            xmlValidator.validateXml(file);
            if (file.isDelta()) {
                List<CaseData> cases = transformService.transform(sftpSshService.readExtractFile(file));

                for (CaseData caseData : cases) {
                    List<CaseDetails> caseByCaseRef = ccdCaseService.findCaseByCaseRef(caseData.getCaseReference());
                    if (caseByCaseRef.isEmpty()) {
                        ccdCasesSender.sendCreateCcdCases(caseData);
                    } else {
                        ccdCasesSender.sendUpdateCcdCases(caseData, caseByCaseRef.get(0));
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
