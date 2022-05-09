
import io.gatling.core.Predef._
import io.gatling.http.Predef._

class RiderDetailsSimulation extends Simulation {

	val httpProtocol = http
		.baseUrl("http://localhost:8080")
		.inferHtmlResources()
		.acceptHeader("*/*")
		.acceptEncodingHeader("gzip, deflate")
		.contentTypeHeader("application/json")
		.userAgentHeader("PostmanRuntime/7.26.8")


	val scn = scenario("EmergencyContactCreateUpdate")
		.exec(http("request_0")
			.get("/profile/details/5fc89292f3ea8a726fef9f7d")
			.header("Content-Type", "application/json")
			.check(status.is(200)))
		.pause(5)
		.repeat(2) {
			exec(http("request_1")
				.get("/profile/details/mob/8888888888")
				.header("Content-Type", "application/json")
				.check(status.is(200)))
		}
	setUp(scn.inject(atOnceUsers(1000))).protocols(httpProtocol)
}