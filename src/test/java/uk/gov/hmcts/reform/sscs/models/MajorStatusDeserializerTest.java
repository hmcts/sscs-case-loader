package uk.gov.hmcts.reform.sscs.models;

import static org.junit.Assert.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.reform.sscs.models.deserialize.gaps2.MajorStatus;

public class MajorStatusDeserializerTest {
    @Test
    public void givenMajorStatusWithTimeZoneDatesAsStrings_shouldSerializeDatesAsZoneDateTimes() throws Exception {
        //Given
        String majorStatusAsString = " {\n"
            + "\"BF_Date\":\"2017-05-23T00:00:00+01:00\",\n"
            + "\"Status_Id\":18,\n"
            + "\"Date_Closed\":\"2017-05-23T15:19:00+01:00\",\n"
            + "\"Date_Set\":\"2017-05-23T15:15:25.15+01:00\"\n"
            + "}";

        //When
        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();
        MajorStatus majorStatus = mapper.readerFor(MajorStatus.class).readValue(majorStatusAsString);
        //Then
        assertNotNull(majorStatus.getDateSet());
    }
}
