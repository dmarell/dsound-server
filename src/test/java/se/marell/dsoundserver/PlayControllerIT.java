/*
 * Created by Daniel Marell 14-06-03 16:51
 */
package se.marell.dsoundserver;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import se.marell.dsoundclient.SoundPlayer;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.util.AssertionErrors.fail;

/**
 * Integration test deploying the app on tomcat and calling the endpoints.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class PlayControllerIT {
    private static final String BASE_URL = "http://localhost:8090";
    RestTemplate restTemplate = new TestRestTemplate();

    @Before
    public void init() {
        restTemplate.delete(BASE_URL + "/cache");
    }

    @Test
    public void shouldNotPlayUnknownSoundClipName() throws Exception {
        SoundPlayer player = new SoundPlayer(BASE_URL);
        try {
            player.play("yes.wav");
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        }
    }

    @Test
    public void shouldPlayTwoClipsGivenSoundData() throws Exception {
        SoundPlayer player = new SoundPlayer(BASE_URL);
        try {
            player.play("yes.wav");
            fail("Expected exception");
        } catch (HttpClientErrorException ignore) {
        }

        player.play("yes.wav", getClass().getResourceAsStream("/yes.wav"));
        List<String> cachedNames = player.getCachedSoundClipNames();
        assertThat(cachedNames.size(), is(1));
        assertThat(cachedNames, contains("yes.wav"));

        player.play("no.wav", getClass().getResourceAsStream("/no.wav"));
        cachedNames = player.getCachedSoundClipNames();
        assertThat(cachedNames.size(), is(2));
        assertThat(cachedNames, containsInAnyOrder("yes.wav", "no.wav"));
    }
}
