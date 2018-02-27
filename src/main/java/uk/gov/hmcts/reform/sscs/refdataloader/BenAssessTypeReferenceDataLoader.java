package uk.gov.hmcts.reform.sscs.refdataloader;

import java.io.InputStream;

import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.exceptions.Gaps2ReferenceDataNotFoundException;
import uk.gov.hmcts.reform.sscs.models.refdata.BenAssessType;

@Service
@Slf4j
public class BenAssessTypeReferenceDataLoader {

    private static final String BEN_ASSESS_TYPE = "Ben_Assess_Type";
    private static final String BEN_ASSESS_TYPE_ID = "BEN_ASSESS_TYPE_ID";
    private static final String BAT_CODE = "BAT_CODE";
    private static final String BAT_DESC = "BAT_DESC";
    private static final String BAT_REF_PREFIX = "BAT_REF_PREFIX";
    private static final String BAT_REF_PREFIX_UT = "BAT_REF_PREFIX_UT";
    private static final String DATA_NOT_FOUND_EXCEPTION = "No BenAssessType found for id = ";

    private final Map<Integer, BenAssessType> benAssessTypeMap = new HashMap<>();

    public void extract(InputStream gaps2RefDataInputStream) throws XMLStreamException {

        if (!benAssessTypeMap.isEmpty()) {
            benAssessTypeMap.clear();
        }

        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

        BenAssessType benAssessType = null;
        String tagContent = null;

        try {
            XMLStreamReader reader =
                xmlInputFactory.createXMLStreamReader(gaps2RefDataInputStream);

            while (reader.hasNext()) {
                int event = reader.next();

                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        if (BEN_ASSESS_TYPE.equals(reader.getLocalName())) {
                            benAssessType = new BenAssessType();
                        }
                        break;

                    case XMLStreamConstants.CHARACTERS:
                        tagContent = reader.getText().trim();
                        break;

                    case XMLStreamConstants.END_ELEMENT:
                        populateBenAssessType(benAssessType, tagContent, reader);
                        break;
                    default:
                        break;
                }

            }

        } catch (XMLStreamException e) {
            log.error("Error in processing Gaps2 BenAssessType reference data", e);
            throw e;
        }
    }

    private void populateBenAssessType(BenAssessType benAssessType, String tagContent, XMLStreamReader reader) {
        if (null != benAssessType) {
            switch (reader.getLocalName()) {
                case BEN_ASSESS_TYPE:
                    benAssessTypeMap.put(benAssessType.getBenAssessTypeId(), benAssessType);
                    break;
                case BEN_ASSESS_TYPE_ID:
                    benAssessType.setBenAssessTypeId(Integer.parseInt(tagContent));
                    break;
                case BAT_CODE:
                    benAssessType.setBatCode(tagContent);
                    break;
                case BAT_DESC:
                    benAssessType.setBatDesc(tagContent);
                    break;
                case BAT_REF_PREFIX:
                    benAssessType.setBatRefPrefix(tagContent);
                    break;
                case BAT_REF_PREFIX_UT:
                    benAssessType.setBatRefPrefixUt(tagContent);
                    break;
                default:
                    break;
            }
        }
    }

    public BenAssessType getBenAssessTypeById(Integer id) {
        if (benAssessTypeMap.containsKey(id)) {
            return benAssessTypeMap.get(id);
        } else {
            throw new Gaps2ReferenceDataNotFoundException(DATA_NOT_FOUND_EXCEPTION + id);
        }
    }

}
