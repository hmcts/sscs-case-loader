

package uk.gov.hmcts.reform.sscs.smoke;

import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("development")
public class GetSavedCase {

    @Value("${sscs.case.loader.url}")
    private String caseLoaderUrl;

    @Test
    public void retrieveCaseFromCcd() {
        RestAssured.baseURI = caseLoaderUrl;
        RestAssured.get("/smoke-test/")
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .assertThat().body("[0].case_data.caseReference", equalTo("SC068/18/01217"));
    }
}



