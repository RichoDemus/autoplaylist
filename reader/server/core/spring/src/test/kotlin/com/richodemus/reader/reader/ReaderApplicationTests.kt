package com.richodemus.reader.reader

import com.richodemus.reader.reader.test.pages.LoginPage
import com.richodemus.reader.reader.test.pages.model.FeedId
import com.richodemus.reader.reader.test.pages.model.FeedUrl
import com.richodemus.reader.youtube_feed_service.YoutubeClient
import com.xebialabs.restito.builder.stub.StubHttp.whenHttp
import com.xebialabs.restito.semantics.Action
import com.xebialabs.restito.semantics.Condition
import com.xebialabs.restito.server.StubServer
import io.restassured.RestAssured
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.glassfish.grizzly.http.util.HttpStatus
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import java.util.UUID
import java.util.concurrent.TimeUnit


@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ReaderApplicationTests {

    @Autowired
    private lateinit var youtubeClient: YoutubeClient

    @LocalServerPort
    var randomServerPort = 0


    lateinit var loginPage: LoginPage

    companion object {
        lateinit var server: StubServer

        @BeforeAll
        @JvmStatic
        internal fun beforeAll() {
            server = StubServer().run()
            RestAssured.port = server.port

            whenHttp(server)
                    .match(Condition.get("/youtube/v3/channels"))
                    .then(Action.status(HttpStatus.OK_200), Action.resourceContent("getChannelResponse.json"))

            whenHttp(server)
                    .match(Condition.get("/youtube/v3/playlistItems"))
                    .then(Action.status(HttpStatus.OK_200), Action.resourceContent("getListItemsResponseTwoVideos.json"))

            whenHttp(server)
                    .match(Condition.get("/youtube/v3/videos"))
                    .then(Action.status(HttpStatus.OK_200), Action.resourceContent("getVideoDetailsResponse.json"))
        }

        @AfterAll
        @JvmStatic
        internal fun afterAll() {
            server.stop()
        }
    }


    @BeforeEach
    internal fun setUp() {
        loginPage = LoginPage(randomServerPort)
        youtubeClient.changeUrl("http://localhost:" + RestAssured.port)
    }

    @Test
    internal fun `Login should fail if user doesn't exist`() {
        val result = loginPage.login(UUID.randomUUID().toString())
        assertThat(result).isFalse()

        assertThat(loginPage.isLoggedIn).isFalse()
    }

    @Test
    internal fun `Should login`() {
        loginPage.createUser()

        val result = loginPage.login()
        assertThat(result).isTrue()

        assertThat(loginPage.isLoggedIn).isTrue()
    }

    @Test
    internal fun `Should not create user if invite code is invalid`() {
        val status = loginPage.createUser(UUID.randomUUID().toString(), "wrong code")
        assertThat(status).isNotEqualTo(200)
        val loggedIn = loginPage.login()
        assertThat(loggedIn).isFalse()
        assertThat(loginPage.isLoggedIn).isFalse()
    }

    @Test
    internal fun `Should not login with wrong password`() {
        loginPage.createUser()

        val result = loginPage.loginWithPassword("wrong password")
        assertThat(result).isFalse()

        assertThat(loginPage.isLoggedIn).isFalse()
    }

    @Test
    internal fun `Usernames should be case insensitive`() {
        loginPage.createUser("lowercase-username")

        val result = loginPage.login(loginPage.username.get().toUpperCase())
        assertThat(result).isTrue()

        assertThat(loginPage.isLoggedIn).isTrue()
    }

    // Todo fix these eventually
//    @Test
//    internal fun `Newly added feed should have special name`() {
//        loginPage.createUser();
//        loginPage.login();
//        val feedPage = loginPage.toFeedPage();
//
//        feedPage.addFeed(FeedUrl("https://www.youtube.com/user/richodemus"));
//
//        val result = feedPage.allFeeds;
//        assertThat(result).extracting("name").containsExactly("UNKNOWN_FEED");
//    }
//
//    @Test
//    internal fun `Newly added feed should contain no items`() {
//        loginPage.createUser()
//        loginPage.login()
//        val feedPage = loginPage.toFeedPage()
//        feedPage.addFeed(FeedUrl("https://www.youtube.com/user/richodemus"))
//
//        val result = feedPage.getItemNames(FeedId("UCyPvQQ-dZmKzh_PrpWmTJkw"))
//
//        assertThat(result).hasSize(0);
//    }

    @Test
    internal fun `Downloaded items should be in feed response`() {
        loginPage.createUser()
        loginPage.login()

        val feedPage = loginPage.toFeedPage()
        feedPage.addFeed(FeedUrl("https://www.youtube.com/user/richodemus"))

        loginPage.downloadFeeds()

        await().atMost(1, TimeUnit.MINUTES).untilAsserted {
            assertThat(feedPage.getItemNames(FeedId("UCyPvQQ-dZmKzh_PrpWmTJkw"))).isNotEmpty()
        }


        assertThat(feedPage.getItemNames(FeedId("UCyPvQQ-dZmKzh_PrpWmTJkw"))).containsExactly("Zs6bAFlcH0M", "vtuDTx1oJGA")
        assertThat(feedPage.allFeeds).extracting("numberOfAvailableItems").containsExactly(2)
    }

    @Test
    internal fun `Should not contain item marked as read`() {
        loginPage.createUser()
        loginPage.login()

        val feedPage = loginPage.toFeedPage()
        feedPage.addFeed(FeedUrl("https://www.youtube.com/user/richodemus"))

        loginPage.downloadFeeds()

        await().atMost(1, TimeUnit.MINUTES).untilAsserted {
            assertThat(feedPage.getItemNames(FeedId("UCyPvQQ-dZmKzh_PrpWmTJkw"))).isNotEmpty()
        }

        assertThat(feedPage.getItemNames(FeedId("UCyPvQQ-dZmKzh_PrpWmTJkw"))).containsExactly("Zs6bAFlcH0M", "vtuDTx1oJGA")

        feedPage.markAsRead(FeedId("UCyPvQQ-dZmKzh_PrpWmTJkw"), "vtuDTx1oJGA")

        assertThat(feedPage.getItemNames(FeedId("UCyPvQQ-dZmKzh_PrpWmTJkw"))).containsExactly("Zs6bAFlcH0M")
        assertThat(feedPage.allFeeds).extracting("numberOfAvailableItems").containsExactly(1)
    }


    @Test
    internal fun `Create label`() {
        loginPage.createUser()
        loginPage.login()

        val feedPage = loginPage.toFeedPage()

        val labels = feedPage.labels
        assertThat(labels).isEmpty()

        val labelId = feedPage.createLabel("my-label")

        val result = feedPage.labels

        assertThat(result).hasSize(1)
        assertThat(result).extracting("name").containsExactly("my-label")
    }

    @Test
    internal fun `Add feed to label`() {
        loginPage.createUser()
        loginPage.login()

        val feedPage = loginPage.toFeedPage()

        val labelId = feedPage.createLabel("my-label")

        feedPage.addFeed(FeedUrl("https://www.youtube.com/user/richodemus"))
        loginPage.downloadFeeds()

        feedPage.addFeedToLabel(FeedId("UCyPvQQ-dZmKzh_PrpWmTJkw"), labelId)

        val result = feedPage.labels
        assertThat(result).flatExtracting("feeds").containsOnly(FeedId("UCyPvQQ-dZmKzh_PrpWmTJkw").toString())
    }
}
