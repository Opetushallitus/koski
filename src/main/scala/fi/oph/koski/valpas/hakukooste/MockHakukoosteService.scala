package fi.oph.koski.valpas.hakukooste

import fi.oph.koski.http.HttpStatus
import fi.oph.koski.valpas.ValpasErrorCategory
import fi.oph.koski.valpas.repository.ValpasHenkilö


class MockHakukoosteService extends ValpasHakukoosteService {
  // Näillä "oideilla" kutsuminen aiheuttaa virhetilanteen (käytetään virhetilanteiden hallinnan testaamiseen)
  val errorOids = Map(
    "unavailable" -> ValpasErrorCategory.unavailable.sure()
  )

  def getHakukoosteet(oppijaOids: Set[ValpasHenkilö.Oid]): Either[HttpStatus, Seq[Hakukooste]] =
    oppijaOids
      .find(errorOids.contains)
      .map(errorOids(_))
      .toLeft { getData(oppijaOids) }

  private def getData(oppijaOids: Set[ValpasHenkilö.Oid]): Seq[Hakukooste] =
    HakukoosteExampleData.data.filter(entry => oppijaOids.contains(entry.oppijaOid))
}
