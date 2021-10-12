package fi.oph.koski.valpas.hakukooste

import fi.oph.koski.http._
import fi.oph.koski.log.Logging
import fi.oph.koski.util.Timing
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasHenkilö

// Dummy-luokka hakukoostekyselyiden disabloimiseksi tarvittaessa kokonaan ympäristöissä
class DisabledHakukoosteService extends ValpasHakukoosteService {
  def getHakukoosteet
    (oppijaOids: Set[ValpasHenkilö.Oid], ainoastaanAktiivisetHaut: Boolean = false, errorClue: String = "")
  : Either[HttpStatus, Seq[Hakukooste]] =
    Right(Seq.empty)
}
