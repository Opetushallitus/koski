package fi.oph.koski.perftest

import fi.oph.koski.json.Json
import fi.oph.koski.json.Json._
import fi.oph.koski.schema.Opiskeluoikeus

object PerfTestData extends App {
  val opiskeluoikeudet = Json.readFile("src/test/resources/opiskeluoikeudet.txt").extract[List[Opiskeluoikeus]]
}
