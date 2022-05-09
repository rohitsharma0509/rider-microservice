//package pivotal

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class PerformanceSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    //.acceptHeader("appl/plain")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Gatling")

  val scn = scenario("PerformanceSimulation")
    .repeat(10) {
      exec(http("GET /profile/5fc76caf16ebc05fa6a2504d").get("/profile/5fc76caf16ebc05fa6a2504d"))
    }

  setUp(
    scn.inject(atOnceUsers(1000))
  ).protocols(httpProtocol)
}


