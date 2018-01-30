package uk.gov.hmcts.reform.sscs.services;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.models.XmlFiles;

import java.util.Optional;

@Service
public class SftpFetchXmlFilesImpl implements FetchXmlFilesService {

    @Override
    public Optional<XmlFiles> fetch() {
        String deltaXml = "src/test/resources/SSCS_Extract_Delta_2017-05-24-16-14-19.xml";
        String refXml = "src/test/resources/SSCS_Extract_Reference_2017-05-24-16-14-19.xml";
        return Optional.of(XmlFiles.builder().delta(deltaXml).ref(refXml).build());
    }

}
