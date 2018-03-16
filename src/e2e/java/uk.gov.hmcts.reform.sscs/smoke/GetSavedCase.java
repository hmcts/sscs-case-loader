package uk.gov.hmcts.reform.sscs.smoke;

import io.restassured.RestAssured;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;



public class GetSavedCase {
    public String sscsCasePattern = "SC068/18/01217";

    private String caseloaderinstance = System.getenv("TEST_URL");

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



