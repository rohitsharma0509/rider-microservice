
import io.gatling.core.Predef._
import io.gatling.http.Predef._

class FetchDrivingLicenceDetails extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .inferHtmlResources()
    .acceptHeader("*/*")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("PostmanRuntime/7.26.8")


  val scn = scenario("FetchDrivingLicenceDetails")
    .exec(http("request_0")
      .get("/profile/1/license-details")
      .header("Content-Type", "application/json"))
    .pause(17)
    .repeat(2) {
      exec(http("request_1")
        .get("/profile/1/license-details")
        .header("Content-Type", "application/json"))
    }

  val fetchDriving = scenario("FetchDrivingLicenceDetails").exec(scn)


  setUp(
    fetchDriving.inject(rampUsers(Integer.getInteger("users", 100)) during (Integer.getInteger("ramp", 1) minutes))
  ).protocols(httpProtocol)


}