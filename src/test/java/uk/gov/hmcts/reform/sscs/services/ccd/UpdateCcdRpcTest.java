package uk.gov.hmcts.reform.sscs.services.ccd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.reform.sscs.ccd.domain.RegionalProcessingCenter;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsCaseData;

@RunWith(JUnitParamsRunner.class)
public class UpdateCcdRpcTest {
    UpdateCcdRpc classUnderTest = null;

    @Before
    public void init() {
        classUnderTest = new UpdateCcdRpc();
    }

    @Test
    public void returnFalseWhenNullGapsData() {
        assertFalse(classUnderTest.updateCcdRpc(null, null));
    }

    @Test
    public void returnFalseWhenNullCcdData() {
        assertFalse(
            classUnderTest.updateCcdRpc(
                SscsCaseData.builder().build(),
                null
            )
        );
    }

    @Test
    public void returnTrueWhenNoCcdData() {
        assertTrue(
            classUnderTest.updateCcdRpc(
                SscsCaseData.builder()
                    .regionalProcessingCenter(RegionalProcessingCenter.builder()
                        .build())
                    .build(),
                SscsCaseData.builder().build()
            )
        );
    }

    @Test
    public void returnTrueWhenCcdDataHasNullRpcName() {
        assertFalse(
            classUnderTest.updateCcdRpc(
                SscsCaseData.builder().build(),
                SscsCaseData.builder()
                    .regionalProcessingCenter(RegionalProcessingCenter.builder()
                        .build())
                    .build()
            )
        );
    }

    @Test
    public void returnTrueWhenCcdDataHasNullRpcAddress() {
        assertTrue(
            classUnderTest.updateCcdRpc(
                SscsCaseData.builder()
                    .regionalProcessingCenter(
                        RegionalProcessingCenter.builder()
                            .name("Some name")
                            .build())
                    .build(),
                SscsCaseData.builder()
                    .regionalProcessingCenter(
                        RegionalProcessingCenter.builder()
                            .name("Some name")
                            .build())
                    .build()
            )
        );
    }

    @Test
    public void returnFalseWhenCcdDataHasSameRpcNameAndAddress() {
        assertFalse(
            classUnderTest.updateCcdRpc(
                SscsCaseData.builder()
                    .regionalProcessingCenter(
                        RegionalProcessingCenter.builder()
                            .name("Some name")
                            .address1("1 Some Street")
                            .build())
                    .build(),
                SscsCaseData.builder()
                    .regionalProcessingCenter(
                        RegionalProcessingCenter.builder()
                            .name("Some name")
                            .address1("1 Some Street")
                            .build())
                    .build()
            )
        );
    }

    @Test
    @Parameters({"Bristol,No", "Glasgow,Yes"})
    public void returnTrueWhenCcdDataHasDifferentRpcName(String rpcName, String expectedIsScottish) throws Exception {
        SscsCaseData gapsCaseData = SscsCaseData.builder()
                .regionalProcessingCenter(
                        RegionalProcessingCenter.builder()
                                .name(rpcName)
                                .address1("1 Some Street")
                                .build())
                .build();

        SscsCaseData existingCcdCaseData = SscsCaseData.builder()
                .regionalProcessingCenter(
                        RegionalProcessingCenter.builder()
                                .name("Some name")
                                .address1("1 Some Street")
                                .build())
                .build();

        assertTrue(classUnderTest.updateCcdRpc(gapsCaseData, existingCcdCaseData));
        assertEquals(gapsCaseData.getRegionalProcessingCenter(), existingCcdCaseData.getRegionalProcessingCenter());
        assertEquals(existingCcdCaseData.getRegionalProcessingCenter().getName(), existingCcdCaseData.getRegion());
        assertEquals(existingCcdCaseData.getIsScottishCase(), expectedIsScottish);
    }

    @Test
    public void returnTrueWhenCcdDataHasDifferentRpcAddress() {
        SscsCaseData gapsCaseData = SscsCaseData.builder()
                .regionalProcessingCenter(
                        RegionalProcessingCenter.builder()
                                .name("Some name")
                                .address1("1 Another Street")
                                .build())
                .build();

        SscsCaseData existingCcdCaseData = SscsCaseData.builder()
                .regionalProcessingCenter(
                        RegionalProcessingCenter.builder()
                                .name("Some name")
                                .address1("1 Some Street")
                                .build())
                .build();
        assertTrue(classUnderTest.updateCcdRpc(gapsCaseData, existingCcdCaseData));
        assertEquals(gapsCaseData.getRegionalProcessingCenter(), existingCcdCaseData.getRegionalProcessingCenter());
        assertEquals(existingCcdCaseData.getRegionalProcessingCenter().getName(), existingCcdCaseData.getRegion());
    }
}
