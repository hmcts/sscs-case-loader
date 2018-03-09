package uk.gov.hmcts.reform.sscs.refdata;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKey.CASE_CODE;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKeyField.CASE_CODE_ID;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKeyField.CCD_KEY;

import org.junit.Before;
import org.junit.Test;

public class RefDataRepositoryTest {

    private RefDataRepository repo;

    @Before
    public void setUp() {
        repo = new RefDataRepository();
    }

    @Test
    public void shouldReturnValueGivenKey() {
        repo.add(CASE_CODE, "A", CASE_CODE_ID, "A");
        repo.add(CASE_CODE, "A", CCD_KEY, "1");

        assertThat(repo.find(CASE_CODE, "A", CASE_CODE_ID), is("A"));
        assertThat(repo.find(CASE_CODE, "A", CCD_KEY), is("1"));
    }
}
