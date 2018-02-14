package uk.gov.hmcts.reform.sscs;

import java.util.ArrayList;
import java.util.List;

import uk.gov.hmcts.reform.sscs.models.serialize.ccd.*;

public final class CaseDataUtils {
    private CaseDataUtils() {
    }

    public static CaseData buildCaseData() {
        Name name = Name.builder()
            .title("Mr")
            .firstName("User")
            .lastName("Test")
            .build();
        Address address = Address.builder()
            .postcode("L17 7AE")
            .build();
        Contact contact = Contact.builder()
            .email("mail@email.com")
            .phone("01234567890")
            .mobile("01234567890")
            .build();
        Identity identity = Identity.builder()
            .dob("1904-03-10")
            .nino("AB 22 55 66 B")
            .build();
        Appellant appellant = Appellant.builder()
            .name(name)
            .address(address)
            .contact(contact)
            .identity(identity)
            .build();
        BenefitType benefitType = BenefitType.builder()
            .code("1325")
            .build();

        HearingOptions hearingOptions = HearingOptions.builder()
            .other("No")
            .build();
        final Appeal appeal = Appeal.builder()
            .mrnDate("2017-10-08")
            .mrnMissingReason("It was missing")
            .appellant(appellant)
            .benefitType(benefitType)
            .hearingOptions(hearingOptions)
            .build();

        Venue venue = Venue.builder()
            .venueTown("Aberdeen")
            .build();
        HearingDetails hearingDetails = HearingDetails.builder()
            .venue(venue)
            .hearingDate("2017-05-24")
            .time("10:45")
            .adjourned("Yes")
            .build();
        Hearing hearings = Hearing.builder()
            .value(hearingDetails)
            .build();
        List<Hearing> hearingsList = new ArrayList<>();
        hearingsList.add(hearings);

        Doc doc = Doc.builder()
            .dateReceived("2017-05-24")
            .description("1")
            .build();
        Documents documents = Documents.builder()
            .value(doc)
            .build();
        List<Documents> documentsList = new ArrayList<>();
        documentsList.add(documents);
        final Evidence evidence = Evidence.builder()
            .documents(documentsList)
            .build();

        DwpTimeExtensionDetails dwpTimeExtensionDetails =  DwpTimeExtensionDetails.builder()
            .requested("Yes")
            .granted("Yes")
            .build();
        DwpTimeExtension dwpTimeExtension = DwpTimeExtension.builder()
            .value(dwpTimeExtensionDetails)
            .build();
        List<DwpTimeExtension> dwpTimeExtensionList = new ArrayList<>();
        dwpTimeExtensionList.add(dwpTimeExtension);

        return CaseData.builder()
            .caseReference("SC068/17/00013")
            .appeal(appeal)
            .hearings(hearingsList)
            .evidence(evidence)
            .dwpTimeExtension(dwpTimeExtensionList)
            .build();
    }
}
