package uk.gov.hmcts.reform.sscs.services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sscs.exceptions.GapsValidationException;
import uk.gov.hmcts.reform.sscs.exceptions.TransformException;
import uk.gov.hmcts.reform.sscs.models.JsonFiles;
import uk.gov.hmcts.reform.sscs.models.XmlFiles;
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
        List<InputStream> inputStreamList = sftpSshService.readExtractFiles();
        log.info("*** case-loader *** Read xml files from SFTP successfully");
        inputStreamList.forEach(inputStream -> {
            // FIXME: 04/02/2018 this method close the intpuStream and causes next service fails
            validateXml(inputStream);
            log.info("*** case-loader *** Validate xml files successfully");
            CaseData caseData = transformXmlFilesToCaseData(inputStream);
            log.info("*** case-loader *** Transform xml files into CCD Cases successfully");
            CaseDetails caseDetails = coreCaseDataService.startEventAndSaveGivenCase(caseData);
            log.info("*** case-loader *** Save Case into CCD  successfully: %s", caseDetails);
        });
    }

    private CaseData transformXmlFilesToCaseData(InputStream inputStream) {
        CaseData caseData;
        try {
            caseData = transformFromXmlFilesToCaseData(inputStream);
        } catch (IOException e) {
            throw new TransformException("Failed to transform xml to CCD data", e);
        }
        return caseData;
    }

    private void validateXml(InputStream inputStream) {
        try {
            xmlValidator.validateXml(inputStream, "Delta");
        } catch (SAXException | XMLStreamException | IOException e) {
            throw new GapsValidationException("Failed to validate xml", e);
        }
    }

    private CaseData transformFromXmlFilesToCaseData(InputStream inputStream) throws IOException {
        JsonFiles jsonCases = transformXmlFilesToJsonFiles.transform(
            buildXmlFilesFromInputStream(inputStream));
        return transformJsonCasesToCaseData.transform(jsonCases.getDelta().toString());
    }

    private XmlFiles buildXmlFilesFromInputStream(InputStream inputStream) throws IOException {
        String xmlAsString = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
        return XmlFiles.builder().delta(xmlAsString).ref("nothing for now").build();
    }

}
