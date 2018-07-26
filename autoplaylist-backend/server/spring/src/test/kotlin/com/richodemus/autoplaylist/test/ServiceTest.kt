package com.richodemus.autoplaylist.test

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.richodemus.autoplaylist.dto.RefreshToken
import com.richodemus.autoplaylist.dto.SpotifyUserId
import com.richodemus.autoplaylist.spotify.AccessToken
import com.richodemus.autoplaylist.spotify.SpotifyPort
import com.richodemus.autoplaylist.spotify.Tokens
import io.github.vjames19.futures.jdk8.Future
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner


@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class ServiceTest {

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @MockBean
    private lateinit var spotifyPort: SpotifyPort

    @Before
    fun setUp() {
        whenever(spotifyPort.getToken("my-fancy-code")).doReturn(Future {
            Tokens(
                    AccessToken("access-token"),
                    "scope",
                    "type",
                    100000,
                    RefreshToken("refresh-token")
            )
        })
        whenever(spotifyPort.getUserId(AccessToken("access-token"))).doReturn(Future {
            SpotifyUserId("spotify-user-id")
        })
    }

    @Test
    fun `smoke test`() {
        val result = restTemplate.getForEntity("/", String::class.java)
        assertThat(result.body).contains("If you can read this you're in the wrong place")
    }

    @Test
    fun `Register new user`() {
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON_UTF8
        }
        val request = HttpEntity("""{"code":"my-fancy-code"}""", headers)
        val result = restTemplate.postForEntity("/v1/sessions", request, String::class.java)
        assertThat(result.statusCode).isEqualTo(OK)
    }
}
