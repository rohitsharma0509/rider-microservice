import io.gatling.core.Predef._
import io.gatling.http.Predef._

class ZoneSaveSimulation extends Simulation {

val putReq = "{\"riderProfileId\":\"1234\",\"preferredZoneId\":\"123\",\"preferredZoneName\":\"Zone test\"}";

val baseURL = Option(System.getProperty("baseURL")) getOrElse """http://localhost:8080"""

	val httpProtocol = http
		.baseUrl(baseURL)
		.inferHtmlResources()
		.acceptHeader("*/*")
		.acceptEncodingHeader("gzip, deflate")
		.contentTypeHeader("application/json")
		.userAgentHeader("PostmanRuntime/7.26.8")

	val headers_0 = Map("Postman-Token" -> "83c9b20d-ce24-4659-8415-7cb302804be9")

	val scn = scenario("ZoneSaveSimulation").exec(http("ZoneSave")
			.post("/profile/preferred-zone")
			.headers(headers_0)
			.body(StringBody(putReq))
			.check(status.is(201)))
			.pause(5)
			.repeat(2){
            exec(http("Zone Save")
            .post("/profile/preferred-zone")
            .body(StringBody(putReq))
            .check(status.is(201)))
            
        }
val Zones = scenario("ZoneSaveSimulation").exec(scn)


    setUp(
        Zones.inject(rampUsers(Integer.getInteger("users", 100)) during (Integer.getInteger("ramp", 1) minutes))
    ).protocols(httpProtocol)
	
}
