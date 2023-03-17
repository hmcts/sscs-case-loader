package uk.gov.hmcts.reform.sscs.services.sftp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class Gaps2FileUtilsTest {

    @Test
    public void shouldOrderTheGaps2FilesBasedOnFileNameDateAndTime() {

        ArrayList<String> list = Lists.newArrayList("SSCS_Extract_Delta_2017-05-24-16-14-19.xml",
            "SSCS_Extract_Delta_2017-06-24-16-14-19.xml",
            "SSCS_Extract_Reference_2017-05-23-16-14-19.xml",
            "SSCS_Extract_Delta_2017-06-24-19-14-19.xml",
            "SSCS_Extract_Reference_2017-06-24-15-14-19.xml",
            "SSCS_Extract_Delta_2017-08-24-16-14-19.xml");

        List<String> orderByDateAndTime = Gaps2FileUtils.getOrderByDateAndTime(list);

        assertThat(orderByDateAndTime.get(0), equalTo("SSCS_Extract_Reference_2017-05-23-16-14-19.xml"));
        assertThat(orderByDateAndTime.get(1), equalTo("SSCS_Extract_Delta_2017-05-24-16-14-19.xml"));
        assertThat(orderByDateAndTime.get(2), equalTo("SSCS_Extract_Reference_2017-06-24-15-14-19.xml"));
        assertThat(orderByDateAndTime.get(3), equalTo("SSCS_Extract_Delta_2017-06-24-16-14-19.xml"));
        assertThat(orderByDateAndTime.get(4), equalTo("SSCS_Extract_Delta_2017-06-24-19-14-19.xml"));
        assertThat(orderByDateAndTime.get(5), equalTo("SSCS_Extract_Delta_2017-08-24-16-14-19.xml"));

    }

    @Test
    public void shouldReturnEmptyList() {
        assertThat(Gaps2FileUtils.getOrderByDateAndTime(Collections.emptyList()),
            equalTo(Collections.EMPTY_LIST));
    }
}
