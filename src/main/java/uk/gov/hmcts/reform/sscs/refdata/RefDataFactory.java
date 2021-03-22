package uk.gov.hmcts.reform.sscs.refdata;

import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKey.BAT_CODE_MAP;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKeyField.BENEFIT_DESC;

import java.io.InputStream;
import java.util.Locale;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.refdata.domain.RefKey;
import uk.gov.hmcts.reform.sscs.refdata.domain.RefKeyField;
import uk.gov.hmcts.reform.sscs.services.refdata.ReferenceDataService;

@Service
@Slf4j
public class RefDataFactory {

    private final ReferenceDataService service;

    @Autowired
    RefDataFactory(ReferenceDataService service) {
        this.service = service;
    }

    public void extract(InputStream refDataInputStream) throws XMLStreamException {

        RefDataRepository repo = new RefDataRepository();

        XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
        xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(refDataInputStream);

        String tagContent = null;
        RefKey key = null;
        String keyId = null;
        while (reader.hasNext()) {
            int event = reader.next();

            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    String localName = reader.getLocalName().toUpperCase(Locale.getDefault());
                    try {
                        key = RefKey.valueOf(localName);
                    } catch (IllegalArgumentException e) { //NOPMD

                    }
                    break;

                case XMLStreamConstants.CHARACTERS:
                    tagContent = reader.getText().trim();
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    localName = reader.getLocalName();
                    try {
                        RefKeyField keyField = RefKeyField.valueOf(localName);
                        if (key != null && key.keyField == keyField) {
                            keyId = tagContent;
                        }
                        repo.add(key, keyId, keyField, tagContent);
                    } catch (IllegalArgumentException e) { //NOPMD

                    }
                    break;
                default:
                    break;
            }
        }
        addBenefitType(repo);
        service.setRefDataRepo(repo);
    }

    private void addBenefitType(RefDataRepository repo) {
        repo.add(BAT_CODE_MAP, "001", BENEFIT_DESC, "UC");
        repo.add(BAT_CODE_MAP, "002", BENEFIT_DESC, "PIP");
        repo.add(BAT_CODE_MAP, "003", BENEFIT_DESC, "PIP");
        repo.add(BAT_CODE_MAP, "037", BENEFIT_DESC, "DLA");
        repo.add(BAT_CODE_MAP, "051", BENEFIT_DESC, "ESA");
        repo.add(BAT_CODE_MAP, "070", BENEFIT_DESC, "Carer's Allowance");
        repo.add(BAT_CODE_MAP, "073", BENEFIT_DESC, "JSA");
    }
}
