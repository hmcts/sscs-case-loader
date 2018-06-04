package uk.gov.hmcts.reform.sscs.smoke;

import io.restassured.RestAssured;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("development")
public class GetSavedCase {

    private final String caseloaderinstance = System.getenv("TEST_URL");

    @Test
    public void retrievecasefromCcd() {
        RestAssured.baseURI = caseloaderinstance;
        RestAssured.useRelaxedHTTPSValidation();

        RestAssured
            .given()
            .when()
            .get("/smoke-test/")
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().asString();
    }
}
