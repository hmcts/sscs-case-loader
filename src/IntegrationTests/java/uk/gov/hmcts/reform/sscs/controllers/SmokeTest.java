package uk.gov.hmcts.reform.sscs.controllers;

import static org.assertj.core.api.BDDAssertions.then;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SmokeTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @LocalServerPort
    private int port;


    @Test
    public void shouldReturn200WhenSendingRequestToController() {

        ResponseEntity<String> entity = testRestTemplate.getForEntity("http://localhost:" + this.port
            + "/smoke-test", String.class);

        then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

}
