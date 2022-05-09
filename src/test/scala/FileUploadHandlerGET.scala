
import io.gatling.core.Predef._
import io.gatling.http.Predef._

class FileUploadHandlerGET extends Simulation {

	val httpProtocol = http
		.baseUrl("http://localhost:8080")
		.inferHtmlResources()
		.acceptHeader("*/*")
		.acceptEncodingHeader("gzip, deflate")
		.userAgentHeader("PostmanRuntime/7.26.8")

	val headers = Map("Content-Type"->"application/json")


	val scn = scenario("FileUploadHandlerGET")
		.exec(http("request_0")
			.get("/profile/5fc89292f3ea8a726fef9f7d/upload?docType=DRIVER_LICENSE")
			.headers(headers))

	setUp(scn.inject(atOnceUsers(1000))).protocols(httpProtocol)
}