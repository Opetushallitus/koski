package fi.oph.koski.ytr

import fi.oph.koski.json.Json

trait YlioppilasTutkintoRekisteri {
  def oppijaByHetu(hetu: String): Option[YtrOppija]
}

object YtrMock extends YlioppilasTutkintoRekisteri {
  implicit val formats = Json.jsonFormats
  def oppijaByHetu(hetu: String): Option[YtrOppija] = Json.readFileIfExists("src/main/resources/mockdata/ytr/" + hetu + ".json").map(_.extract[YtrOppija])
}