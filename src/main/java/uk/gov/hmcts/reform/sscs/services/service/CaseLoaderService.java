package uk.gov.hmcts.reform.sscs.services.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.xml.stream.XMLStreamException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import uk.gov.hmcts.reform.sscs.models.JsonFiles;
import uk.gov.hmcts.reform.sscs.models.XmlFiles;
import uk.gov.hmcts.reform.sscs.services.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.sscs.services.mapper.TransformJsonCasesToCaseData;
import uk.gov.hmcts.reform.sscs.services.mapper.TransformXmlFilesToJsonFiles;
import uk.gov.hmcts.reform.sscs.services.sftp.SftpSshService;
import uk.gov.hmcts.reform.sscs.services.xml.XmlValidator;

@Service
@Slf4j
public class CaseLoaderService {

    @Autowired
    private SftpSshService sftpSshService;
    @Autowired
    private XmlValidator xmlValidator;
    @Autowired
    private TransformXmlFilesToJsonFiles transformXmlFilesToJsonFiles;
    @Autowired
    private TransformJsonCasesToCaseData transformJsonCasesToCaseData;
    @Autowired
    private CoreCaseDataService coreCaseDataService;


    public void process() throws IOException, SAXException, XMLStreamException {
        List<InputStream> inputStreamList = sftpSshService.readExtractFiles();
        Optional<XmlFiles> optionalXmlFiles;
        try {
            optionalXmlFiles = Optional.of(XmlFiles.builder().delta(convert(inputStreamList.get(0),
                StandardCharsets.UTF_8)).build());
        } catch (IndexOutOfBoundsException ie) {
            optionalXmlFiles = Optional.empty();
            log.error(ie.getMessage());
        }

        if (optionalXmlFiles.isPresent()) {
            XmlFiles xmlFiles = optionalXmlFiles.get();
            validateXmls(xmlFiles);
            JsonFiles jsonFiles = transformXmlFilesToJsonFiles.transform(xmlFiles);
            coreCaseDataService.startEventAndSaveGivenCase(transformJsonCasesToCaseData.transform(jsonFiles
                .getDelta().toString()));
        }
    }

    private String convert(InputStream inputStream, Charset charset) throws IOException {

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, charset))) {
            return br.lines().collect(Collectors.joining(System.lineSeparator()));
        } finally {
            inputStream.close();
        }
    }

    private void validateXmls(XmlFiles xmlFiles) throws IOException, SAXException, XMLStreamException {
        xmlValidator.validateXml(xmlFiles.getDelta());
        xmlValidator.validateXml(xmlFiles.getRef());
    }
}
