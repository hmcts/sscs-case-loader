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
import uk.gov.hmcts.reform.sscs.models.refdata.AppealStatus;

@Service
@Slf4j
public class AppealStatusReferenceDataLoader {

    private static final String APPEAL_STATUS = "Appeal_Status";
    private static final String APPEAL_STATUS_ID = "APPEAL_STATUS_ID";
    private static final String APS_MINOR = "APS_MINOR";
    private static final String APS_BF_DAYS = "APS_BF_DAYS";
    private static final String APS_DESC = "APS_DESC";
    private static final String APS_DORMANT = "APS_DORMANT";
    private static final String APS_SEARCH = "APS_SEARCH";
    private static final String DATA_NOT_FOUND_EXCEPTION = "No AppealStatus found for ID = ";

    private final Map<Integer, AppealStatus> appealStatusMap = new HashMap<>();

    public void extract(InputStream gaps2RefDataInputStream) throws XMLStreamException {

        if (!appealStatusMap.isEmpty()) {
            appealStatusMap.clear();
        }

        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

        AppealStatus appealStatus = null;
        String tagContent = null;

        try {
            XMLStreamReader reader =
                xmlInputFactory.createXMLStreamReader(gaps2RefDataInputStream);

            while (reader.hasNext()) {
                int event = reader.next();

                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        if (APPEAL_STATUS.equals(reader.getLocalName())) {
                            appealStatus = new AppealStatus();
                        }
                        break;

                    case XMLStreamConstants.CHARACTERS:
                        tagContent = reader.getText().trim();
                        break;

                    case XMLStreamConstants.END_ELEMENT:
                        populateAppealStatus(appealStatus, tagContent, reader);
                        break;
                    default:
                        break;
                }

            }

        } catch (XMLStreamException e) {
            log.error("Error in processing Gaps2 Appeal Status reference data", e);
            throw e;
        }
    }

    private void populateAppealStatus(AppealStatus appealStatus, String tagContent, XMLStreamReader reader) {
        if (null != appealStatus) {
            switch (reader.getLocalName()) {
                case APPEAL_STATUS:
                    appealStatusMap.put(appealStatus.getAppealStatusId(), appealStatus);
                    break;
                case APPEAL_STATUS_ID:
                    appealStatus.setAppealStatusId(Integer.parseInt(tagContent));
                    break;
                case APS_MINOR:
                    appealStatus.setApsMinor(Integer.parseInt(tagContent));
                    break;
                case APS_BF_DAYS:
                    appealStatus.setApsBfDays(Integer.parseInt(tagContent));
                    break;
                case APS_DESC:
                    appealStatus.setApsDesc(tagContent);
                    break;
                case APS_DORMANT:
                    appealStatus.setApsDormant(tagContent);
                    break;
                case APS_SEARCH:
                    appealStatus.setApsSearch(tagContent);
                    break;
                default:
                    break;
            }
        }
    }

    public AppealStatus getAppealStatusById(Integer id) {
        if (appealStatusMap.containsKey(id)) {
            return appealStatusMap.get(id);
        } else {
            throw new Gaps2ReferenceDataNotFoundException(DATA_NOT_FOUND_EXCEPTION + id);
        }
    }

}
