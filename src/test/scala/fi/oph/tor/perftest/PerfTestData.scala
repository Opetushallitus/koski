package fi.oph.tor.perftest

import fi.oph.tor.json.Json
import fi.oph.tor.schema.OpiskeluOikeus
import fi.oph.tor.json.Json._

object PerfTestData extends App {
  val opiskeluoikeudet = Json.readFile("src/test/resources/opiskeluoikeudet.txt").extract[List[OpiskeluOikeus]]
}
