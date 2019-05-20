package uk.gov.hmcts.reform.sscs.services.mapper;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sscs.ccd.domain.*;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.AppealCase;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.Parties;

@Service
@Slf4j
public class TransformAppealCaseToCaseData {
    public static final int APPELLANT_ROLE_ID = 4;
    public static final int REP_ROLE_ID = 3;
    public static final int APPOINTEE_ROLE_ID = 24;

    @Value("${rpc.venue.id.enabled}")
    private boolean lookupRpcByVenueId;

    private final CaseDataBuilder caseDataBuilder;

    @Autowired
    TransformAppealCaseToCaseData(CaseDataBuilder caseDataBuilder) {
        this.caseDataBuilder = caseDataBuilder;
    }

    public SscsCaseData transform(final AppealCase appealCase) {
        List<Parties> parties = appealCase.getParties();

        Optional<Parties> appellantParty = (parties == null) ? Optional.empty() :
            parties.stream().filter(f -> f.getRoleId() == APPELLANT_ROLE_ID).findFirst();

        if (!appellantParty.isPresent()) {
            log.error("An appeal, for caseId {}, exists without an appellant. This cannot be possible.",
                appealCase.getAppealCaseId());
        }

        Optional<Parties> representativeParty = (parties == null) ? Optional.empty() :
            parties.stream().filter(f -> f.getRoleId() == REP_ROLE_ID).findFirst();

        Optional<Parties> appointeeParty = (parties == null) ? Optional.empty() :
            parties.stream().filter(f -> f.getRoleId() == APPOINTEE_ROLE_ID).findFirst();

        BenefitType benefitType = caseDataBuilder.buildBenefitType(appealCase);

        Appeal appeal = getAppeal(appealCase, appellantParty, appointeeParty, representativeParty, benefitType);

        List<Hearing> hearingsList = caseDataBuilder.buildHearings(appealCase);

        Evidence evidence = caseDataBuilder.buildEvidence(appealCase);

        List<DwpTimeExtension> dwpTimeExtensionList = caseDataBuilder.buildDwpTimeExtensions(appealCase);

        List<Event> events = caseDataBuilder.buildEvent(appealCase);
        RegionalProcessingCenter regionalProcessingCenter = appellantParty.map((Parties party) ->
            regionalProcessingCenter(party, appealCase)).orElse(null);
        return SscsCaseData.builder()
            .caseReference(appealCase.getAppealCaseRefNum())
            .appeal(appeal)
            .hearings(hearingsList)
            .regionalProcessingCenter(regionalProcessingCenter)
            .region((regionalProcessingCenter != null) ? regionalProcessingCenter.getName() : null)
            .evidence(evidence)
            .dwpTimeExtension(dwpTimeExtensionList)
            .events(events)
            .generatedNino(appeal.getAppellant() != null ? appeal.getAppellant().getIdentity().getNino() : null)
            .generatedSurname(appeal.getAppellant() != null ? appeal.getAppellant().getName().getLastName() : null)
            .generatedEmail(appeal.getAppellant() != null ? appeal.getAppellant().getContact().getEmail() : null)
            .generatedMobile(appeal.getAppellant() != null ? appeal.getAppellant().getContact().getMobile() : null)
            .generatedDob(appeal.getAppellant() != null ? appeal.getAppellant().getIdentity().getDob() : null)
            .subscriptions(caseDataBuilder.buildSubscriptions(
                appellantParty, representativeParty, appointeeParty, appealCase.getAppealCaseRefNum())
            )
            .ccdCaseId(appealCase.getAdditionalRef())
            .build();
    }

    private Appeal getAppeal(final AppealCase appealCase,
                             final Optional<Parties> appellantParty,
                             final Optional<Parties> appointeeParty,
                             final Optional<Parties> representativeParty,
                             final BenefitType benefitType) {
        return Appeal.builder()
                .appellant(appellantParty.map(
                    (Parties party) -> appellant(party, appointeeParty, appealCase)).orElse(null)
                )
                .benefitType(benefitType)
                .hearingOptions(appellantParty.map((Parties party) -> hearingOptions(party, appealCase)).orElse(null))
                .hearingType(HearingType.getHearingTypeByTribunalsTypeId(appealCase.getTribunalTypeId()).getValue())
                .rep(representativeParty.map(this::representative).orElse(null))
                .build();
    }

    private RegionalProcessingCenter regionalProcessingCenter(final Parties appellantParty,
                                                              final AppealCase appealCase) {
        RegionalProcessingCenter regionalProcessingCenter = null;
        if (lookupRpcByVenueId) {
            regionalProcessingCenter = caseDataBuilder.buildRegionalProcessingCentre(appealCase, appellantParty);
        }
        return regionalProcessingCenter;
    }

    private Appellant appellant(final Parties appellantParty,
                                final Optional<Parties> appointeeParty,
                                final AppealCase appealCase) {
        return Appellant.builder()
            .name(caseDataBuilder.buildName(appellantParty))
            .contact(caseDataBuilder.buildContact(appellantParty))
            .identity(caseDataBuilder.buildIdentity(appellantParty, appealCase))
            .appointee(appointeeParty.map((Parties party) -> appointee(party, appealCase)).orElse(null))
            .build();
    }

    private Appointee appointee(final Parties appointeeParty, final AppealCase appealCase) {
        return Appointee.builder()
            .name(caseDataBuilder.buildName(appointeeParty))
            .contact(caseDataBuilder.buildContact(appointeeParty))
            .identity(caseDataBuilder.buildIdentity(appointeeParty, appealCase))
            .build();
    }

    private HearingOptions hearingOptions(final Parties appellantParty, final AppealCase appealCase) {
        return caseDataBuilder.buildHearingOptions(appellantParty, appealCase.getTribunalTypeId());
    }

    private Representative representative(final Parties representativeParty) {
        return Representative.builder()
            .hasRepresentative("Yes")
            .contact(caseDataBuilder.buildContact(representativeParty))
            .name(caseDataBuilder.buildName(representativeParty))
            .build();
    }

}
