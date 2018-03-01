package uk.gov.hmcts.reform.sscs.refdata;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKey.ADMIN_TEAM;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKey.CASE_CODE;
import static uk.gov.hmcts.reform.sscs.refdata.domain.RefKeyField.CASE_CODE_ID;

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
        repo.add("CASE_CODE", "CASE_CODE_ID", "A");
        repo.add("ADMIN_TEAM", "CASE_CODE_ID", "B");

        assertThat(repo.find(CASE_CODE, CASE_CODE_ID), is("A"));
        assertThat(repo.find(ADMIN_TEAM, CASE_CODE_ID), is("B"));
    }
}
