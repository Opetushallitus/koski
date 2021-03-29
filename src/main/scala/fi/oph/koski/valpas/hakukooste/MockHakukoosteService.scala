package fi.oph.koski.valpas.hakukooste

import fi.oph.koski.http.HttpStatus
import fi.oph.koski.valpas.ValpasErrorCategory
import fi.oph.koski.valpas.henkilo.ValpasMockOppijat
import fi.oph.koski.valpas.repository.{ValpasHenkilö, ValpasHenkilöLaajatTiedot}


class MockHakukoosteService extends ValpasHakukoosteService {
  // Näillä oideilla kutsuminen aiheuttaa virhetilanteen (käytetään virhetilanteiden hallinnan testaamiseen)
  private def errorOids = Map(
    "unavailable" -> ValpasErrorCategory.unavailable.sure(),
    ValpasMockOppijat.lukionAloittanut.oid -> ValpasErrorCategory.unavailable.sure()
  )

  def getHakukoosteet(oppijaOids: Set[ValpasHenkilö.Oid]): Either[HttpStatus, Seq[Hakukooste]] =
    if (oppijaOids.forall(errorOids.contains)) {
      Left(HttpStatus.fold(oppijaOids.map(errorOids(_))))
    } else {
      Right(getData(oppijaOids))
    }

  private def getData(oppijaOids: Set[ValpasHenkilö.Oid]): Seq[Hakukooste] =
    HakukoosteExampleData.data.filter(entry => oppijaOids.contains(entry.oppijaOid))
}
