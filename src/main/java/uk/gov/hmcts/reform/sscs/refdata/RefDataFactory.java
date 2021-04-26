package uk.gov.hmcts.reform.sscs.refdata;

import static java.util.Arrays.stream;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKey.BAT_CODE_MAP;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKeyField.BENEFIT_DESC;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.Benefit;
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
        BiConsumer<Benefit, String> refDataRepoBiConsumer = getRefDataRepoBiConsumer(repo);

        stream(Benefit.values())
            .filter(benefit -> isNotEmpty(benefit.getCaseLoaderKeyId()))
            .forEach(addEachBenefitCaseLoaderKeyIdToRefDataRepository(refDataRepoBiConsumer));
    }

    private BiConsumer<Benefit, String> getRefDataRepoBiConsumer(RefDataRepository repo) {
        return (Benefit benefit, String caseloaderKeyId) ->
            repo.add(BAT_CODE_MAP, caseloaderKeyId, BENEFIT_DESC, benefit.getShortName());
    }

    private Consumer<Benefit> addEachBenefitCaseLoaderKeyIdToRefDataRepository(
        BiConsumer<Benefit, String> refDataRepoBiConsumer) {
        return benefit -> benefit.getCaseLoaderKeyId()
                            .forEach(caseLoaderKeyId -> refDataRepoBiConsumer.accept(benefit, caseLoaderKeyId));
    }
}
