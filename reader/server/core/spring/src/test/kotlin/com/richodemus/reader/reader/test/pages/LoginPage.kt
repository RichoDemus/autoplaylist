package com.richodemus.reader.reader.test.pages

import com.google.common.collect.ImmutableMap
import io.restassured.RestAssured
import org.assertj.core.api.Assertions
import java.util.Map
import java.util.Optional
import java.util.UUID

class LoginPage(port: Int) {
    private val baseUrl: String
    var token: String? = null
    var username: String? = null
    private var session = Map.of<String, String?>()
    fun createUser() {
        val username = UUID.randomUUID().toString()
        val status = createUser(username, DEFAULT_PASSWORD, INVITE_CODE)
        Assertions.assertThat(status).isEqualTo(200)
    }

    fun createUser(username: String): Int {
        val status = createUser(username, DEFAULT_PASSWORD, INVITE_CODE)
        Assertions.assertThat(status).isEqualTo(200)
        return status
    }

    fun createUser(username: String, inviteCode: String): Int {
        return createUser(username, DEFAULT_PASSWORD, inviteCode)
    }

    private fun createUser(username: String, password: String, inviteCode: String): Int {
        this.username = username
        return RestAssured
                .given().contentType("application/json")
                .body(ImmutableMap.builder<Any, Any>()
                        .put("username", username).put("password", password)
                        .put("inviteCode", inviteCode)
                        .build())
                .`when`().post("$baseUrl/v1/users")
                .then().extract().statusCode()
    }

    fun loginWithPassword(password: String): Boolean {
        return login(username!!, password)
    }

    @JvmOverloads
    fun login(username: String = this.username!!, password: String = DEFAULT_PASSWORD): Boolean {
        this.username = username
        val then = RestAssured
                .given().contentType("application/json").body("{\"username\":\"$username\",\"password\":\"$password\"}")
                .`when`().post("$baseUrl/v1/sessions")
                .then()
        session = then.extract().cookies()
        return then.extract().statusCode() == 200
    }

    fun downloadFeeds() {
        RestAssured
                .given().cookies(session)
                .post("$baseUrl/v1/admin/download")
                .then().statusCode(200)
    }

    fun refreshToken() {
        val token = token!!
        val user = username!!
        this.token = RestAssured
                .given().header("x-token-jwt", token).body("")
                .`when`().post("$baseUrl/v1/users/$user/sessions/refresh")
                .then().extract().body().jsonPath().get<String>("token")
    }

    val isLoggedIn: Boolean
        get() {
            val then = RestAssured
                    .given().cookies(session)
                    .`when`()["$baseUrl/v1/sessions"]
                    .then()
            return then.extract().statusCode() == 200
        }

    fun toFeedPage(): FeedPage {
        return FeedPage(
                baseUrl,
                username?:throw RuntimeException("No username"),
                session)
    }

    companion object {
        private const val DEFAULT_PASSWORD = "123456789qwertyuio123qweasd"
        private const val INVITE_CODE = "testcode"
    }

    init {
        baseUrl = "http://localhost:$port"
    }
}
