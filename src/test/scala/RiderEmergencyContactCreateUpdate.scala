
import io.gatling.core.Predef._
import io.gatling.http.Predef._

class RiderEmergencyContactCreateUpdate extends Simulation {

	val httpProtocol = http
		.baseUrl("http://localhost:8080")
		.inferHtmlResources()
		.acceptHeader("*/*")
		.acceptEncodingHeader("gzip, deflate")
		.contentTypeHeader("application/json")
		.userAgentHeader("PostmanRuntime/7.26.8")


	val scn = scenario("EmergencyContactCreateUpdate")
		.exec(http("request_0")
			.post("/profile/emergency-contact")
			.header("Content-Type", "application/json")
			.body(RawFileBody("src/test/resources/recordedsimulation/emergency_contact_request.json"))
			.check(status.is(200)))

		.exec(http("request_4")
			.get("/profile/emergency-contact/5fc89292f3ea8a726fef9f7d")
			.header("Content-Type", "application/json")
			.check(status.is(200)))

	setUp(scn.inject(atOnceUsers(100))).protocols(httpProtocol)
}