/*
 * Created by Daniel Marell 14-09-26 07:37
 */
package se.marell.dsoundserver;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;

/**
 * Integration test deploying the app on tomcat and calling the endpoint /appversion.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class AppVersionIT {
    @Test
    public void shouldGetAppVersion() throws Exception {
        RestTemplate template = new RestTemplate();

        ResponseEntity<String> entity = template.getForEntity(
                "http://localhost:8090/version",
                String.class);

        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(BuildInfo.getAppVersion(), entity.getBody());
    }
}
