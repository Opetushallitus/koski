package fi.oph.koski.valpas.hakukooste

import com.typesafe.config.Config
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http._
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasHenkilö
import fi.oph.koski.valpas.oppija.ValpasErrorCategory

// Dummy-luokka hakukoostekyselyiden disabloimiseksi tarvittaessa kokonaan ympäristöissä
class DisabledHakukoosteService(application: KoskiApplication) extends ValpasHakukoosteService {
  protected val localizationRepository = application.valpasLocalizationRepository

  def getHakukoosteet
    (oppijaOids: Set[ValpasHenkilö.Oid], ainoastaanAktiivisetHaut: Boolean = false, errorClue: String = "")
  : Either[HttpStatus, Seq[Hakukooste]] =
    Left(ValpasErrorCategory.unavailable.sure(
      "Hakukoosteita ei toistaiseksi saada haettua suoritusrekisteristä."
    ))
}
