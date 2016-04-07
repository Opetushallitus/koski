package fi.oph.tor.perftest

import fi.oph.tor.json.Json
import fi.oph.tor.json.Json._
import fi.oph.tor.schema.Opiskeluoikeus

object PerfTestData extends App {
  val opiskeluoikeudet = Json.readFile("src/test/resources/opiskeluoikeudet.txt").extract[List[Opiskeluoikeus]]
}
