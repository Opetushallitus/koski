package fi.oph.koski.valpas.hakukooste

import scala.collection.immutable.HashMap

class MockHakukoosteService extends ValpasHakukoosteService {
  // Näillä "oideilla" kutsuminen aiheuttaa virhetilanteen (käytetään virhetilanteiden hallinnan testaamiseen)
  val errorOids = HashMap(
    "timeout" -> "502 Timeout"
  )

  def getHakukoosteet(oppijaOids: Set[String]): Either[ValpasHakukoosteServiceError, Seq[Hakukooste]] =
    oppijaOids
      .find(errorOids.contains)
      .map(errorOids(_))
      .toLeft { getData(oppijaOids) }

  private def getData(oppijaOids: Set[String]): Seq[Hakukooste] =
    HakukoosteExampleData.data.filter(entry => oppijaOids.contains(entry.oppijaOid))
}

class MockEmptyHakukoosteService extends ValpasHakukoosteService {
  def getHakukoosteet(oppijaOids: Set[String]): Either[ValpasHakukoosteServiceError, Seq[Hakukooste]] = Right(List())
}
