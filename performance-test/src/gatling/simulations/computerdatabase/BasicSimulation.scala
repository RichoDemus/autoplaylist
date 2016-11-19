package computerdatabase

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.collection.immutable.HashMap
import scala.util.Random

class BasicSimulation extends Simulation {
  val httpConf = http // 4
    .baseURL("http://localhost:8080") // 5
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8") // 6
    .doNotTrackHeader("1")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0")

  val normalUsers = scenario("BasicSimulation")
    .feed(UsernameFeeder.username)
    .feed(FeedNameFeeder.feedName)
    .exec(
      MainPage.mainPage,
      Signup.signup,
      Login.login,
      GetFeeds.getFeeds,
      AddFeed.addFeed,
      repeat(5) {
        exec(
          GetFeeds.getFeeds,
          GetFeed.getFeed,
          WatchItem.watchItem)
      })

  setUp(
    normalUsers.inject(constantUsersPerSec(80) during 120)
  ).protocols(httpConf)
}

object MainPage {
  val mainPage = exec(http("MainPage") // let's give proper names, as they are displayed in the reports
    .get("/"))
    .pause(3)
}

object Signup {
  val signup = exec(http("Signup")
    .post("/api/users/")
    .body(StringBody(session => s"""{"username":"${session("username").as[String]}", "password":"asdasd", "inviteCode":"iwouldlikeaninvitepleaseletmesignuptotestthis"}""")).asJSON)
    .pause(1)
}

object Login {
  val login = exec(http("Login")
    .post(session => "/api/users/".concat(session("username").as[String]).concat("/sessions/"))
    .body(StringBody("asdasd"))
    .check(jsonPath("$.token").saveAs("token")))
    .pause(1)
}

object GetFeeds {
  val getFeeds = exec(http("Get All Feeds")
    .get(session => "/api/users/".concat(session("username").as[String]).concat("/feeds/"))
    .header("x-token-jwt", session => session("token").as[String])
    .check(jsonPath("$.feeds").saveAs("feeds")))
    .pause(2)
}

object AddFeed {
  val addFeed = exec(http("Add Feed")
    .post(session => "/api/users/".concat(session("username").as[String]).concat("/feeds/"))
    .header("x-token-jwt", session => session("token").as[String])
    .body(StringBody(session => session("feed").as[String])))
    .pause(1)
}

object GetFeed {
  val getFeed = exec(http("Get Feed")
    .get(session => "/api/users/".concat(session("username").as[String]).concat("/feeds/").concat(session("feed").as[String]))
    .header("x-token-jwt", session => session("token").as[String]))
    .pause(1)
}

object WatchItem {
  private val intToInt: HashMap[String, String] = collection.immutable.HashMap("AHughman08" -> "drujIxWmxyc", "valve" -> "P8D3eoMII-w", "Isinona" -> "NQA18gY0rOI", "brokeeats" -> "29wQhPiLUhA")

  def asd(qwe: String): String = {
    intToInt(qwe)
  }

  val watchItem = exec(http("Watch Item")
    .post(session => "/api/users/".concat(session("username").as[String]).concat("/feeds/").concat(session("feed").as[String]).concat("/items/").concat(asd(session("feed").as[String])))
    .header("x-token-jwt", session => session("token").as[String])
    .body(StringBody("""{ "action": "MARK_READ" }""")).asJSON)
    .pause(10)
}

object UsernameFeeder {
  val username = Iterator.continually(
    Map("username" -> "User-".concat(Random.nextInt(Integer.MAX_VALUE).toString))
  )
}

object FeedNameFeeder {
  val A = List("AHughman08", "valve", "Isinona", "brokeeats")
  val feedName = Iterator.continually(
    Map("feed" -> A(Random.nextInt(A.size)))
  )
}

