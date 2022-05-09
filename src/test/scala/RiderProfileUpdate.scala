import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import io.gatling.http.request.builder.HttpRequestBuilder.toActionBuilder

class RiderProfileUpdate extends Simulation {

    val putReq = "{\"id\":\"5feac624de3fe23d2472007f\",\"firstName\":\"Rohit\",\"lastName\":\"Sharma\",\"address\":{\"landmark\":\"landmark\",\"city\":\"Bangkok\",\"country\":\"Thailand\",\"village\":\"village\",\"district\":\"district\",\"state\":\"state\",\"countryCode\":\"TH\",\"zipCode\":\"203205\",\"floorNumber\":\"1234\",\"unitNumber\":\"unitNumber\"},\"dob\":\"20/12/1988\",\"nationalID\":\"12345776790\",\"accountNumber\":\"121212123312121\",\"consentAcceptFlag\":true,\"dataSharedFlag\":true}"


    val httpConf: HttpProtocolBuilder = http
      .baseUrl("http://localhost:8080")
      .acceptHeader("application/json")
      .userAgentHeader("Mozilla/4.0(compatible;IE)")


  val scn: ScenarioBuilder = scenario("RiderProfileUpdate")
   // .feed(randomSession)
    .exec(http("activate request")
      .put("/profile")
      .header("Content-Type", "application/json")
      .body(StringBody(putReq))
      .check(status.is(200)))
    .pause(5)
	.repeat(2){
            exec(http("activate request")
            .put("/profile")
            .header("Content-Type", "application/json")
            .body(StringBody(putReq))
            .check(status.is(200)))
            
        }


val riderProfile: ScenarioBuilder = scenario("RiderProfileUpdation").exec(scn)


    setUp(
        riderProfile.inject(rampUsers(Integer.getInteger("users", 100)) during (Integer.getInteger("ramp", 1) minutes))
    ).protocols(httpConf)
	


  }