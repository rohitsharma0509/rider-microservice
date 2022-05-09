
import io.gatling.core.Predef._
import io.gatling.http.Predef._

class DrivingLicenseDetailsCreateUpdate extends Simulation {

	val httpProtocol = http
		.baseUrl("http://localhost:8080")
		.inferHtmlResources()
		.acceptHeader("*/*")
		.acceptEncodingHeader("gzip, deflate")
		.contentTypeHeader("application/json")
		.userAgentHeader("PostmanRuntime/7.26.8")


	val scn = scenario("DrivingLicenseDetailsCreateUpdate")
		.exec(http("request_0")
			.post("/profile/1/license-details")
			.header("Content-Type", "application/json")
			.body(RawFileBody("src/test/resources/recordedsimulation/drivinglicensecreateupdate/0000_request.json"))
			.check(status.is(201)))
			.pause(5)
			.repeat(2){
            exec(http("Zone Save")
            .post("/profile/1/license-details")
            .body(RawFileBody("src/test/resources/recordedsimulation/drivinglicensecreateupdate/0000_request.json"))
            .check(status.is(201)))
            .pause(5)
            .exec(http("request_4")
			.put("/profile/1/license-details")
			.header("Content-Type", "application/json")
			.body(RawFileBody("src/test/resources/recordedsimulation/drivinglicensecreateupdate/0000_request.json"))
			.check(status.is(200)))
            
        }
	
	val drivingLicenseDetails = scenario("DrivingLicenseDetailsCreateUpdate").exec(scn)


    setUp(
        drivingLicenseDetails.inject(rampUsers(Integer.getInteger("users", 100)) during (Integer.getInteger("ramp", 1) minutes))
    ).protocols(httpProtocol)
			
	
}