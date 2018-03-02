package uk.gov.hmcts.reform.sscs.refdata;

import java.io.InputStream;
import java.util.Locale;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.refdata.domain.RefKey;
import uk.gov.hmcts.reform.sscs.refdata.domain.RefKeyField;

@Service
public class RefDataFactory {

    private final RefDataRepository repo;

    @Autowired
    public RefDataFactory(RefDataRepository repo) {
        this.repo = repo;
    }

    public void extract(InputStream refDataInputStream) throws XMLStreamException {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(refDataInputStream);

        String tagContent = null;
        RefKey key = null;
        while (reader.hasNext()) {
            int event = reader.next();

            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    String localName = reader.getLocalName().toUpperCase(Locale.getDefault());
                    try {
                        key = RefKey.valueOf(localName);
                    } catch (IllegalArgumentException e) {
                        // Not a reference tag name
                    }
                    break;

                case XMLStreamConstants.CHARACTERS:
                    tagContent = reader.getText().trim();
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    localName = reader.getLocalName();
                    try {
                        RefKeyField keyField = RefKeyField.valueOf(localName);
                        repo.add(key, keyField, tagContent);
                    } catch (IllegalArgumentException e) {
                        // Not a reference field tag name
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
