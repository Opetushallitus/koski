package fi.oph.koski.valpas.hakukooste

import fi.oph.koski.http.HttpStatus
import fi.oph.koski.valpas.ValpasErrorCategory
import fi.oph.koski.valpas.opiskeluoikeusfixture.ValpasMockOppijat
import fi.oph.koski.valpas.opiskeluoikeusrepository.{ValpasHenkilö, ValpasHenkilöLaajatTiedot}


class MockHakukoosteService extends ValpasHakukoosteService {
  // Näillä oideilla kutsuminen aiheuttaa virhetilanteen (käytetään virhetilanteiden hallinnan testaamiseen)
  private def errorOids = Map(
    "unavailable" -> ValpasErrorCategory.unavailable.sure(),
    ValpasMockOppijat.hakukohteidenHakuEpäonnistuu.oid -> ValpasErrorCategory.unavailable.sure()
  )

  def getHakukoosteet(
    oppijaOids: Set[ValpasHenkilö.Oid],
    ainoastaanAktiivisetHaut: Boolean,
    errorClue: String
  ): Either[HttpStatus, Seq[Hakukooste]] =
    if (oppijaOids.forall(errorOids.contains)) {
      Left(HttpStatus.fold(oppijaOids.map(errorOids(_))))
    } else {
      Right(getData(oppijaOids, ainoastaanAktiivisetHaut))
    }

  private def getData(oppijaOids: Set[ValpasHenkilö.Oid], ainoastaanAktiivisetHaut: Boolean): Seq[Hakukooste] =
    HakukoosteExampleData.data.filter(entry =>
      oppijaOids.contains(entry.oppijaOid) &&
      (!ainoastaanAktiivisetHaut || !entry.aktiivinenHaku.isDefined || entry.aktiivinenHaku.get)
    )
}
