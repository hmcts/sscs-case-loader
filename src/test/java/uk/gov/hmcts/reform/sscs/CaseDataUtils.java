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

        HearingOptions hearingOptions = HearingOptions.builder()
            .languageInterpreter("Yes")
            .other("No")
            .build();
        Appeal appeal = Appeal.builder()
            .mrnDate("2017-10-08")
            .mrnMissingReason("It was missing")
            .appellant(appellant)
            .hearingOptions(hearingOptions)
            .build();

        Venue venue = Venue.builder()
            .venueTown("Aberdeen")
            .build();
        Hearing hearing = Hearing.builder()
            .venue(venue)
            .hearingDate("2017-05-24")
            .build();
        Value value = Value.builder()
            .value(hearing)
            .build();

        List<Value> valueList = new ArrayList<>();
        valueList.add(value);


        return CaseData.builder()
            .caseReference("SC068/17/00013")
            .appeal(appeal)
            .hearings(valueList)
            .build();
    }
}
