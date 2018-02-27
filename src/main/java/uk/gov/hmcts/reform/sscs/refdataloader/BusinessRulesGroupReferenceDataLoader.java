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
import uk.gov.hmcts.reform.sscs.models.refdata.BusinessRulesGroup;

@Service
@Slf4j
public class BusinessRulesGroupReferenceDataLoader {


    private static final String BUSINESS_RULES_GROUP = "Business_Rules_Group";
    private static final String BUSINESS_RULES_GRP_ID = "BUSINESS_RULES_GRP_ID";
    private static final String BRG_SHORT_DESC = "BRG_SHORT_DESC";
    private static final String BRG_LONG_DESC = "BRG_LONG_DESC";
    private static final String TRIBUNAL_ID = "TRIBUNAL_ID";
    private static final String JURISDICTION_ID = "JURISDICTION_ID";
    private static final String DATA_NOT_FOUND_EXCEPTION = "No BusinessRulesGroup found for id = ";

    private final Map<Integer, BusinessRulesGroup> businessRulesGroupMap = new HashMap<>();

    public void extract(InputStream gaps2RefDataInputStream) throws XMLStreamException {

        if (!businessRulesGroupMap.isEmpty()) {
            businessRulesGroupMap.clear();
        }

        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

        BusinessRulesGroup businessRulesGroup = null;
        String tagContent = null;

        try {
            XMLStreamReader reader =
                xmlInputFactory.createXMLStreamReader(gaps2RefDataInputStream);

            while (reader.hasNext()) {
                int event = reader.next();

                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        if (BUSINESS_RULES_GROUP.equals(reader.getLocalName())) {
                            businessRulesGroup = new BusinessRulesGroup();
                        }
                        break;

                    case XMLStreamConstants.CHARACTERS:
                        tagContent = reader.getText().trim();
                        break;

                    case XMLStreamConstants.END_ELEMENT:
                        populateBusinessRuleGroup(businessRulesGroup, tagContent, reader);
                        break;
                    default:
                        break;
                }

            }

        } catch (XMLStreamException e) {
            log.error("Error in processing Gaps2 Business rules group reference data", e);
            throw e;
        }
    }

    private void populateBusinessRuleGroup(BusinessRulesGroup businessRulesGroup,
                                           String tagContent,
                                           XMLStreamReader reader) {
        if (null != businessRulesGroup) {
            switch (reader.getLocalName()) {
                case BUSINESS_RULES_GROUP:
                    businessRulesGroupMap.put(businessRulesGroup.getBusinessRulesGrpId(), businessRulesGroup);
                    break;
                case BUSINESS_RULES_GRP_ID:
                    businessRulesGroup.setBusinessRulesGrpId(Integer.parseInt(tagContent));
                    break;
                case BRG_SHORT_DESC:
                    businessRulesGroup.setBrgShortDesc(tagContent);
                    break;
                case BRG_LONG_DESC:
                    businessRulesGroup.setBrgLongDesc(tagContent);
                    break;
                case TRIBUNAL_ID:
                    businessRulesGroup.setTribunalId(Integer.parseInt(tagContent));
                    break;
                case JURISDICTION_ID:
                    businessRulesGroup.setJurisdictionId(Integer.parseInt(tagContent));
                    break;
                default:
                    break;
            }
        }
    }

    public BusinessRulesGroup getBusinessRulesGroupById(Integer id) {
        if (businessRulesGroupMap.containsKey(id)) {
            return businessRulesGroupMap.get(id);
        } else {
            throw new Gaps2ReferenceDataNotFoundException(DATA_NOT_FOUND_EXCEPTION + id);
        }
    }

}
