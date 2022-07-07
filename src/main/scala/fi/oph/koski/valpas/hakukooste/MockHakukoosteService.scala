package fi.oph.koski.valpas.hakukooste

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.valpas.opiskeluoikeusfixture.ValpasMockOppijat
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasHenkilö
import fi.oph.koski.valpas.oppija.ValpasErrorCategory


class MockHakukoosteService(application: KoskiApplication) extends ValpasHakukoosteService {
  protected val localizationRepository = application.valpasLocalizationRepository

  // Näillä oideilla kutsuminen aiheuttaa virhetilanteen (käytetään virhetilanteiden hallinnan testaamiseen)
  private def errorOids = Map(
    "unavailable" -> ValpasErrorCategory.unavailable.sure(),
    ValpasMockOppijat.hakukohteidenHakuEpäonnistuu.oid -> ValpasErrorCategory.unavailable.sure()
  )

  private def failsWholeFetchOid = ValpasMockOppijat.sureHautAinaEpäonnistuvaOppija

  def getHakukoosteet(
    oppijaOids: Set[ValpasHenkilö.Oid],
    ainoastaanAktiivisetHaut: Boolean,
    errorClue: String
  ): Either[HttpStatus, Seq[Hakukooste]] = {
    if (oppijaOids.contains(failsWholeFetchOid.oid)) {
      Left(ValpasErrorCategory.unavailable.sure())
    } else if (oppijaOids.forall(errorOids.contains)) {
      Left(HttpStatus.fold(oppijaOids.map(errorOids(_))))
    } else {
      Right(getData(oppijaOids, ainoastaanAktiivisetHaut))
    }
  }

  private def getData(oppijaOids: Set[ValpasHenkilö.Oid], ainoastaanAktiivisetHaut: Boolean): Seq[Hakukooste] =
    HakukoosteExampleData.data.filter(entry =>
      oppijaOids.contains(entry.oppijaOid) &&
      (!ainoastaanAktiivisetHaut || entry.aktiivinenHaku.isEmpty || entry.aktiivinenHaku.get)
    )
}
