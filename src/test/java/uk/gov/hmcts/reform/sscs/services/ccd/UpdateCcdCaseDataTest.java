package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.sscs.CaseDetailsUtils.getSscsCaseDetails;
import static uk.gov.hmcts.reform.sscs.services.ccd.CcdCasesSenderTest.CASE_DETAILS_WITH_APPEAL_RECEIVED_JSON;
import static uk.gov.hmcts.reform.sscs.services.ccd.CcdCasesSenderTest.buildCaseData;

import java.io.IOException;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.sscs.ccd.domain.DwpTimeExtension;
import uk.gov.hmcts.reform.sscs.ccd.domain.DwpTimeExtensionDetails;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseDetails;
import uk.gov.hmcts.reform.sscs.models.GapsEvent;
import uk.gov.hmcts.reform.sscs.models.UpdateType;

@RunWith(MockitoJUnitRunner.class)
public class UpdateCcdCaseDataTest {

    @Mock
    private UpdateCcdAppellantData updateCcdAppellantData;

    private UpdateCcdCaseData updateCcdCaseData;
    private SscsCaseData gapsCaseData;
    private SscsCaseDetails existingCaseDetails;

    @Before
    public void setUp() {
        updateCcdCaseData = new UpdateCcdCaseData(updateCcdAppellantData);
    }

    @Test
    public void givenAnEventChange_shouldUpdateEventsInExistingCcdCase() throws IOException {
        gapsCaseData = buildCaseData(GapsEvent.RESPONSE_RECEIVED);
        existingCaseDetails = getSscsCaseDetails(CASE_DETAILS_WITH_APPEAL_RECEIVED_JSON);

        assertEquals(1, existingCaseDetails.getData().getEvents().size());
        assertEquals(2, gapsCaseData.getEvents().size());
        assertThat(gapsCaseData.getEvents().toArray(),
            not(equalTo(existingCaseDetails.getData().getEvents().toArray())));

        given(updateCcdAppellantData.updateCcdAppellantData(gapsCaseData, existingCaseDetails.getData()))
            .willReturn(false);

        UpdateType updateType = updateCcdCaseData.updateCcdRecordForChangesAndReturnUpdateType(gapsCaseData,
            existingCaseDetails.getData());

        assertThat(gapsCaseData.getEvents().toArray(), equalTo(existingCaseDetails.getData().getEvents().toArray()));
        assertThat(updateType, is(UpdateType.EVENT_UPDATE));
    }

    @Test
    public void givenGapsAppealDataIsNull_shouldNotUpdateData() {
        gapsCaseData = SscsCaseData.builder()
            .appeal(null)
            .events(Collections.emptyList())
            .build();

        existingCaseDetails = SscsCaseDetails.builder()
            .data(SscsCaseData.builder()
                .events(Collections.emptyList())
                .build())
            .build();

        UpdateType updateType = updateCcdCaseData.updateCcdRecordForChangesAndReturnUpdateType(
            gapsCaseData, existingCaseDetails.getData());

        assertThat(updateType, is(UpdateType.NO_UPDATE));
    }

    //TODO add scenarios for when dwpTime is empty, null, and different data.

    @Test
    public void givenDwpTimeExtensionChanged_shouldUpdateData() {
        gapsCaseData = SscsCaseData.builder()
            .dwpTimeExtension(Collections.singletonList(DwpTimeExtension.builder()
                .value(DwpTimeExtensionDetails.builder()
                    .granted("yes")
                    .build())
                .build()))
            .events(Collections.emptyList())
            .build();

        existingCaseDetails = SscsCaseDetails.builder()
            .data(SscsCaseData.builder()
                .events(Collections.emptyList())
                .build())
            .build();

        UpdateType updateType = updateCcdCaseData.updateCcdRecordForChangesAndReturnUpdateType(
            gapsCaseData, existingCaseDetails.getData());

        assertThat(updateType, is(UpdateType.DATA_UPDATE));
        assertThat(existingCaseDetails.getData().getDwpTimeExtension(), equalTo(gapsCaseData.getDwpTimeExtension()));
    }

    //TODO cover DATA_UPDATE flow

    //TODO test dwpTimeExtension

    //TODO cover dataChange scenarios


}
