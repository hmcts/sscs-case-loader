package uk.gov.hmcts.reform.sscs.refdataloader;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.exceptions.Gaps2ReferenceDataNotFoundException;
import uk.gov.hmcts.reform.sscs.models.refdata.AdminTeam;


@Service
@Slf4j
public class AdminTeamReferenceDataLoader {

    private static final String ADMIN_TEAM = "Admin_Team";
    private static final String ADMIN_TEAM_ID = "ADMIN_TEAM_ID";
    private static final String ADM_NAME = "ADM_NAME";
    private static final String ADM_CLOSURE_DATE = "ADM_CLOSURE_DATE";
    private static final String VENUE_ID = "VENUE_ID";
    private static final String ADMIN_TEAM_CODE = "ADMIN_TEAM_CODE";
    private static final String ADMIN_TEAM_CODE_2 = "ADMIN_TEAM_CODE_2";
    private static final String DATA_NOT_FOUND_EXCEPTION = "No AdminTeam found for ID = ";

    private final Map<Integer, AdminTeam> adminTeamMap = new HashMap<>();

    public void extract(InputStream gaps2RefDataInputStream) throws XMLStreamException {

        if (!adminTeamMap.isEmpty()) {
            adminTeamMap.clear();
        }

        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

        AdminTeam adminTeam = null;
        String tagContent = null;

        try {
            XMLStreamReader reader =
                xmlInputFactory.createXMLStreamReader(gaps2RefDataInputStream);

            while (reader.hasNext()) {
                int event = reader.next();

                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        if (ADMIN_TEAM.equals(reader.getLocalName())) {
                            adminTeam = new AdminTeam();
                        }
                        break;

                    case XMLStreamConstants.CHARACTERS:
                        tagContent = reader.getText().trim();
                        break;

                    case XMLStreamConstants.END_ELEMENT:
                        populateAdminTeam(adminTeam, tagContent, reader);
                        break;
                    default:
                        break;
                }

            }

        } catch (XMLStreamException e) {
            log.error("Error in processing Gaps2 Admin Team reference data", e);
            throw e;
        }
    }

    private void populateAdminTeam(AdminTeam adminTeam, String tagContent, XMLStreamReader reader) {
        if (null != adminTeam) {
            switch (reader.getLocalName()) {
                case ADMIN_TEAM:
                    adminTeamMap.put(adminTeam.getAdminTeamId(), adminTeam);
                    break;
                case ADMIN_TEAM_ID:
                    adminTeam.setAdminTeamId(Integer.parseInt(tagContent));
                    break;
                case ADM_NAME:
                    adminTeam.setAdmName(tagContent);
                    break;
                case ADM_CLOSURE_DATE:
                    adminTeam.setAdmClosureDate(LocalDateTime.parse(tagContent,
                        DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                    break;
                case VENUE_ID:
                    adminTeam.setVenueId(Integer.parseInt(tagContent));
                    break;
                case ADMIN_TEAM_CODE:
                    adminTeam.setAdminTeamCode(tagContent);
                    break;
                case ADMIN_TEAM_CODE_2:
                    adminTeam.setAdminTeamCode2(tagContent);
                    break;
                default:
                    break;
            }
        }
    }

    public AdminTeam getAdminTeamsById(Integer id) {
        if (adminTeamMap.containsKey(id)) {
            return adminTeamMap.get(id);
        } else {
            throw new Gaps2ReferenceDataNotFoundException(DATA_NOT_FOUND_EXCEPTION + id);
        }
    }

}
