package com.richodemus.autoplaylist.test

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.test.context.junit4.SpringRunner


@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class ServiceTest {

    @Autowired
    private val restTemplate: TestRestTemplate? = null

//    @MockBean
//    private val spotifyPort: SpotifyPort? = null

    @Test
    fun `smoke test`() {
        val asd = this.restTemplate!!.getForEntity("/", String::class.java)
        assertThat(asd.body).contains("If you can read this you're in the wrong place")
    }
}
