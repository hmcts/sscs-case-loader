package uk.gov.hmcts.reform.sscs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import uk.gov.hmcts.reform.sscs.models.serialize.ccd.*;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.subscriptions.Subscription;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.subscriptions.Subscriptions;

public final class CaseDataUtils {
    private CaseDataUtils() {
    }

    public static CaseData buildCaseData(String caseReference) {
        Name name = Name.builder()
            .title("Mr")
            .firstName("User")
            .lastName("Test")
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
            .contact(contact)
            .identity(identity)
            .build();
        BenefitType benefitType = BenefitType.builder()
            .code("1325")
            .build();

        DateRange dateRange = DateRange.builder()
            .start("2018-06-30")
            .end("2018-06-30")
            .build();
        ExcludeDate excludeDate = ExcludeDate.builder()
            .value(dateRange)
            .build();

        HearingOptions hearingOptions = HearingOptions.builder()
            .wantsToAttend("Yes")
            .arrangements(Arrays.asList("disabledAccess", "hearingLoop"))
            .excludeDates(Collections.singletonList(excludeDate))
            .other("No")
            .build();

        MrnDetails mrnDetails = MrnDetails.builder()
            .mrnDate("2018-06-30")
            .dwpIssuingOffice("1")
            .build();

        Representative representative = Representative.builder()
            .hasRepresentative("Yes")
            .build();

        final Appeal appeal = Appeal.builder()
            .appellant(appellant)
            .benefitType(benefitType)
            .hearingOptions(hearingOptions)
            .mrnDetails(mrnDetails)
            .rep(representative)
            .signer("Signer")
            .build();

        Address venueAddress = Address.builder()
            .line1("12 The Road Avenue")
            .line2("Village")
            .town("Aberdeen")
            .county("Aberdeenshire")
            .postcode("AB12 0HN")
            .build();

        Venue venue = Venue.builder()
            .name("The venue")
            .address(venueAddress)
            .googleMapLink("http://www.googlemaps.com/aberdeenvenue")
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
            .evidenceType("Medical evidence")
            .evidenceProvidedBy("Appellant")
            .build();
        Documents documents = Documents.builder()
            .value(doc)
            .build();
        List<Documents> documentsList = new ArrayList<>();
        documentsList.add(documents);
        final Evidence evidence = Evidence.builder()
            .documents(documentsList)
            .build();

        DwpTimeExtensionDetails dwpTimeExtensionDetails = DwpTimeExtensionDetails.builder()
            .requested("Yes")
            .granted("Yes")
            .build();
        DwpTimeExtension dwpTimeExtension = DwpTimeExtension.builder()
            .value(dwpTimeExtensionDetails)
            .build();
        List<DwpTimeExtension> dwpTimeExtensionList = new ArrayList<>();
        dwpTimeExtensionList.add(dwpTimeExtension);

        Event event = Event.builder()
            .type("appealCreated")
            .description("Appeal Created")
            .date("2001-12-14T21:59:43.10")
            .build();
        Events events = Events.builder()
            .value(event)
            .build();

        Subscription appellantSubscription = Subscription.builder()
            .tya("")
            .email("")
            .mobile("")
            .subscribeEmail("NO")
            .subscribeSms("NO")
            .reason("")
            .build();
        Subscription supporterSubscription = Subscription.builder()
            .tya("")
            .email("")
            .mobile("")
            .subscribeEmail("")
            .subscribeSms("")
            .reason("")
            .build();
        Subscriptions subscriptions = Subscriptions.builder()
            .appellantSubscription(appellantSubscription)
            .supporterSubscription(supporterSubscription)
            .build();

        return CaseData.builder()
            .caseReference(caseReference)
            .appeal(appeal)
            .hearings(hearingsList)
            .evidence(evidence)
            .dwpTimeExtension(dwpTimeExtensionList)
            .events(Collections.singletonList(events))
            .generatedNino(identity.getNino())
            .generatedSurname(name.getLastName())
            .generatedEmail(contact.getEmail())
            .generatedMobile(contact.getMobile())
            .subscriptions(subscriptions)
            .build();
    }
}
