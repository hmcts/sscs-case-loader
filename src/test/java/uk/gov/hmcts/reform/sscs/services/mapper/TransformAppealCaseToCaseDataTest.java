package uk.gov.hmcts.reform.sscs.services.mapper;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.AppealCase;
import uk.gov.hmcts.reform.sscs.models.refdata.RegionalProcessingCenter;
import uk.gov.hmcts.reform.sscs.models.serialize.ccd.CaseData;
import uk.gov.hmcts.reform.sscs.services.date.DateHelper;
import uk.gov.hmcts.reform.sscs.services.refdata.ReferenceDataService;
import uk.gov.hmcts.reform.sscs.services.refdata.RegionalProcessingCenterService;

public class TransformAppealCaseToCaseDataTest {

    @Test
    public void givenACaseData_shouldBeTransformToCaseDataWithSubscriptionsAndAppealNumber() throws Exception {
        ReferenceDataService referenceDataService = mock(ReferenceDataService.class);
        CaseDataEventBuilder caseDataEventBuilder = mock(CaseDataEventBuilder.class);
        RegionalProcessingCenterService regionalProcessingCenterService = mock(RegionalProcessingCenterService.class);
        CaseDataBuilder caseDataBuilder =
            new CaseDataBuilder(referenceDataService, caseDataEventBuilder, regionalProcessingCenterService);
        TransformAppealCaseToCaseData transformAppealCaseToCaseData =
            new TransformAppealCaseToCaseData(caseDataBuilder);
        ReflectionTestUtils.setField(transformAppealCaseToCaseData, "lookupRpcByVenueId", true);

        String expectedRegionName = "region-name";
        RegionalProcessingCenter expectedRegionalProcessingCentre = RegionalProcessingCenter.builder()
            .name(expectedRegionName)
            .build();
        when(regionalProcessingCenterService.getByVenueId("68")).thenReturn(expectedRegionalProcessingCentre);

        AppealCase appealCase = getAppealCase();

        CaseData caseData = transformAppealCaseToCaseData.transform(appealCase);

        String appealNumber = caseData.getSubscriptions().getAppellantSubscription().getTya();
        assertTrue("appealNumber length is not 10 digits", appealNumber.length() == 10);
        assertEquals("Appeal references are mapped (SC Reference)", "SC068/17/00013", caseData.getCaseReference());
        assertEquals("Appeal references are mapped (CCD ID)", "1111222233334444", caseData.getCcdCaseId());
        assertThat(caseData.getRegionalProcessingCenter(), is(expectedRegionalProcessingCentre));
        assertThat(caseData.getRegion(), is(expectedRegionName));

        String dob = DateHelper.getValidDateOrTime(appealCase.getParties().get(0).getDob(), true);

        assertThat(caseData.getGeneratedDob(), is(dob));
    }

    private AppealCase getAppealCase() throws Exception {
        ObjectMapper mapper = Jackson2ObjectMapperBuilder
            .json()
            .indentOutput(true)
            .build();

        String appealCaseJson =
            IOUtils.toString(
                TransformAppealCaseToCaseDataTest.class
                    .getClassLoader()
                    .getResourceAsStream("AppealCase.json"),
                StandardCharsets.UTF_8.name()
            );

        return mapper.readerFor(AppealCase.class).readValue(appealCaseJson);
    }
}
