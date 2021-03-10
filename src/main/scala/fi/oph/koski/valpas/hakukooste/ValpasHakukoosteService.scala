package fi.oph.koski.valpas.hakukooste

import fi.oph.koski.config.KoskiApplication

trait ValpasHakukoosteService {
  type ValpasHakukoosteServiceError = String

  def getHakukoosteet(oppijaOids: Set[String]): Either[ValpasHakukoosteServiceError, Seq[Hakukooste]]
}

object ValpasHakukoosteService {
  def apply(application: KoskiApplication): ValpasHakukoosteService = {
    application.config.getString("opintopolku.virkailija.url") match {
      case "mock" => new MockHakukoosteService()
      case _ => new MockEmptyHakukoosteService()
    }
  }
}
