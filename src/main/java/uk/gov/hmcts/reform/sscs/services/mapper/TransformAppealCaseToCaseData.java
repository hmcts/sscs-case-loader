package uk.gov.hmcts.reform.sscs.services.mapper;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.AppealCase;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Parties;

@Service
public class TransformAppealCaseToCaseData {

    @Value("${rpc.venue.id.enabled}")
    private boolean lookupRpcByVenueId;

    private final CaseDataBuilder caseDataBuilder;

    @Autowired
    TransformAppealCaseToCaseData(CaseDataBuilder caseDataBuilder) {
        this.caseDataBuilder = caseDataBuilder;
    }

    public SscsCaseData transform(AppealCase appealCase) {
        List<Parties> parties = appealCase.getParties();

        Optional<Parties> party = (parties == null) ? Optional.empty() :
            parties.stream().filter(f -> f.getRoleId() == 4).findFirst();

        Name name = null;
        Contact contact = null;
        Identity identity = null;
        HearingOptions hearingOptions = null;
        String generatedNino = "";
        String generatedSurname = "";
        String generatedEmail = "";
        String generatedMobile = "";
        String generatedDob = "";
        Appellant appellant = null;
        RegionalProcessingCenter regionalProcessingCenter = null;
        String region = null;

        if (party.isPresent()) {
            name = caseDataBuilder.buildName(party.get());
            contact = caseDataBuilder.buildContact(party.get());
            identity = caseDataBuilder.buildIdentity(party.get(), appealCase);
            hearingOptions = caseDataBuilder.buildHearingOptions(party.get(), appealCase.getTribunalTypeId());
            generatedNino = identity.getNino();
            generatedSurname = name.getLastName();
            generatedEmail = contact.getEmail();
            generatedMobile = contact.getMobile();
            generatedDob = identity.getDob();

            appellant = Appellant.builder()
                .name(name)
                .contact(contact)
                .identity(identity)
                .build();

            if (lookupRpcByVenueId) {
                regionalProcessingCenter = caseDataBuilder.buildRegionalProcessingCentre(appealCase, party.get());
                region = (regionalProcessingCenter != null) ? regionalProcessingCenter.getName() : null;
            }
        }

        BenefitType benefitType = caseDataBuilder.buildBenefitType(appealCase);

        Appeal appeal = Appeal.builder()
            .appellant(appellant)
            .benefitType(benefitType)
            .hearingOptions(hearingOptions)
            .hearingType(HearingType.getHearingTypeByTribunalsTypeId(appealCase.getTribunalTypeId()).getValue())
            .build();

        List<Hearing> hearingsList = caseDataBuilder.buildHearings(appealCase);

        Evidence evidence = caseDataBuilder.buildEvidence(appealCase);

        List<DwpTimeExtension> dwpTimeExtensionList = caseDataBuilder.buildDwpTimeExtensions(appealCase);

        List<Event> events = caseDataBuilder.buildEvent(appealCase);
        return SscsCaseData.builder()
            .caseReference(appealCase.getAppealCaseRefNum())
            .appeal(appeal)
            .hearings(hearingsList)
            .regionalProcessingCenter(regionalProcessingCenter)
            .region(region)
            .evidence(evidence)
            .dwpTimeExtension(dwpTimeExtensionList)
            .events(events)
            .generatedNino(generatedNino)
            .generatedSurname(generatedSurname)
            .generatedEmail(generatedEmail)
            .generatedMobile(generatedMobile)
            .generatedDob(generatedDob)
            .subscriptions(caseDataBuilder.buildSubscriptions())
            .ccdCaseId(appealCase.getAdditionalRef())
            .build();
    }

}
