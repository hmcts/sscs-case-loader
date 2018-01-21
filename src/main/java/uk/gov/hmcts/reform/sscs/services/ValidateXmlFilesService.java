package uk.gov.hmcts.reform.sscs.services;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.models.XmlFiles;

@Service
public class ValidateXmlFilesService {
    public boolean validate(XmlFiles xmlFiles) {
        return true;
    }
}
