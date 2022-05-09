
import io.gatling.core.Predef._
import io.gatling.http.Predef._

class RiderTrainingDetails extends Simulation {

	val httpProtocol = http
		.baseUrl("http://localhost:8080")
		.inferHtmlResources()
		.acceptHeader("*/*")
		.acceptEncodingHeader("gzip, deflate")
		.contentTypeHeader("application/json")
		.userAgentHeader("PostmanRuntime/7.26.8")


	val scn = scenario("RiderTrainingDetails")
		.exec(http("request_0")
			.post("/profile/training/5fd8843718c9d426903f3851/appointment")
			.header("Content-Type", "application/json")
			.body(RawFileBody("src/test/resources/recordedsimulation/training/training_appointment_dto.json"))
			.check(status.is(200)))

		.exec(http("request_4")
			.get("/profile/training/5fd8843718c9d426903f3851/appointment")
			.header("Content-Type", "application/json")
			.check(status.is(200)))

	setUp(scn.inject(atOnceUsers(100))).protocols(httpProtocol)
}