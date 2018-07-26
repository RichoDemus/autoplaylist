package com.richodemus.autoplaylist.test.pages

import io.restassured.RestAssured
import io.restassured.http.ContentType.JSON

class LoginPage(private val port: Int) {
    fun login(code: String): MainPage {
        val sessionId = RestAssured
                .given().body("""{"code":"$code"}""").contentType(JSON)
                .`when`().post("http://localhost:$port/v1/sessions")
                .then().assertThat().statusCode(200).extract().detailedCookie("JSESSIONID")
        return MainPage(port, sessionId)
    }
}
