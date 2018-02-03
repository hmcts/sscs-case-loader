package uk.gov.hmcts.reform.sscs.services;

import java.io.InputStream;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
        inputStreamList.forEach(inputStream -> {
            // TODO: 03/02/2018 change services to take inputStream as argument
            //xmlValidator.validateXml(inputStream);
            //XmlFiles.builder().delta(inputStream);
            //JsonFiles jsonFiles = transformXmlFilesToJsonFiles.transform(xmlFiles);
            //coreCaseDataService.startEventAndSaveGivenCase(transformJsonCasesToCaseData.transform(jsonFiles
            //.getDelta().toString()));
        });
    }
}
