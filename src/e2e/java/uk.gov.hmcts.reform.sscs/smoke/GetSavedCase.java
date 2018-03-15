

package uk.gov.hmcts.reform.sscs.smoke;

import io.restassured.RestAssured;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("development")
public class GetSavedCase {
    public String sscsCasePattern = "SC068/18/01217";


    @Value("${caseloaderinstance}")
    private String caseloaderinstance;
    @Test
    public void retrievecasefromCCD() {
        RestAssured.baseURI=caseloaderinstance;


        String response = RestAssured
                .given()
                .when()
                .get("/smoke-test/")
                .then()
                .statusCode(HttpStatus.OK.value())
                .and()
                .extract().body().asString();
        System.out.println(response);
        assertThat(response).contains(sscsCasePattern);


    }
}



