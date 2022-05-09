
import io.gatling.core.Predef._
import io.gatling.http.Predef._

class VehicleRegistrationFetchAPI extends Simulation {

	val httpProtocol = http
		.baseUrl("http://localhost:8080")
		.inferHtmlResources()
		.acceptHeader("*/*")
		.acceptEncodingHeader("gzip, deflate")
		.contentTypeHeader("application/json")
		.userAgentHeader("PostmanRuntime/7.26.8")

	val headers = Map("Content-Type"->"application/json")

	val scn = scenario("VehicleRegistrationFetchAPI")
		.exec(http("request_0")
			.get("/profile/10004/vehicle-details")
			.headers(headers))
		.pause(4)
		.exec(http("request_1")
			.get("/profile/10004/vehicle-details")
			.headers(headers))
		.pause(5)
		.exec(http("request_2")
			.get("/profile/10002/vehicle-details")
			.headers(headers))
		.pause(4)
		.exec(http("request_3")
			.get("/profile/10001/vehicle-details")
			.headers(headers))

	setUp(scn.inject(atOnceUsers(1000))).protocols(httpProtocol)
}