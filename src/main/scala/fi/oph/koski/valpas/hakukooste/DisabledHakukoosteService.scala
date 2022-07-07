package fi.oph.koski.valpas.hakukooste

import fi.oph.koski.http._
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasHenkilö
import fi.oph.koski.valpas.oppija.ValpasErrorCategory

// Dummy-luokka hakukoostekyselyiden disabloimiseksi tarvittaessa kokonaan ympäristöissä
class DisabledHakukoosteService extends ValpasHakukoosteService {
  def getHakukoosteet
    (oppijaOids: Set[ValpasHenkilö.Oid], ainoastaanAktiivisetHaut: Boolean = false, errorClue: String = "")
  : Either[HttpStatus, Seq[Hakukooste]] =
    Left(ValpasErrorCategory.unavailable.sure(
      "Hakukoosteita ei toistaiseksi saada haettua suoritusrekisteristä."
    ))
}
