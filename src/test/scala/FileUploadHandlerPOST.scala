
import io.gatling.core.Predef._
import io.gatling.http.Predef._

class FileUploadHandlerPOST extends Simulation {

	val httpProtocol = http
		.baseUrl("http://localhost:8080")
		.inferHtmlResources()
		.acceptHeader("*/*")
		.acceptEncodingHeader("gzip, deflate")
		.contentTypeHeader("multipart/form-data; boundary=--------------------------535439233464734247232818")
		.userAgentHeader("PostmanRuntime/7.26.8")


	val scn = scenario("FileUploadHandlerPOST")
		.exec(http("request_0")
			.post("/profile/5fc89292f3ea8a726fef9f7d/upload?docType=DRIVER_LICENSE")
			.body(RawFileBody("src/test/resources/recordedsimulation/fileuploadhandlerpost/0000_request.dat")))
			
	setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
}