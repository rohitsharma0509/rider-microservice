import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder.toActionBuilder

class RiderProfileCreation extends Simulation {

   val postReq = "{\"firstName\":\"Rohit\",\"lastName\":\"Sharma\",\"address\":{\"landmark\":\"landmark\",\"city\":\"Bangkok\",\"country\":\"Thailand\",\"village\":\"village\",\"district\":\"district\",\"state\":\"state\",\"countryCode\":\"TH\",\"zipCode\":\"203205\",\"floorNumber\":\"1234\",\"unitNumber\":\"unitNumber\"},\"dob\":\"20/12/1988\",\"nationalID\":\"123456790\",\"accountNumber\":\"1212121212121\",\"phoneNumber\":\"8899999999\",\"consentAcceptFlag\":true,\"dataSharedFlag\":true}";

    val httpConf = http
      .baseUrl("http://localhost:8080")
      .acceptHeader("application/json")
      .userAgentHeader("Mozilla/4.0(compatible;IE)")

    val scn = scenario("Activate")
     // .feed(randomSession)
      .exec(http("activate request")
        .post("/profile")
        .header("Content-Type", "application/json")
        .body(StringBody(postReq))
        .check(status.is(201)))
      .pause(5)

   setUp(
    scn.inject(atOnceUsers(5))
  ).protocols(httpConf)
  }