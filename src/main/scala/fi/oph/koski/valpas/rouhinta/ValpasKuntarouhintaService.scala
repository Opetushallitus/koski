package fi.oph.koski.valpas.rouhinta

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.DatabaseConverters
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.Logging
import fi.oph.koski.util.Timing
import fi.oph.koski.valpas.valpasuser.ValpasSession

class ValpasKuntarouhintaService(application: KoskiApplication) extends DatabaseConverters with Logging with Timing {
  private val oppijaService = application.valpasOppijaService

  def haeKunnanPerusteella
    (kunta: String)
    (implicit session: ValpasSession)
  : Either[HttpStatus, KuntarouhinnanTulos] = {
    val oppivelvollisetKoskessa = oppijaService.getOppivelvollisetKotikunnallaIlmanOikeustarkastusta(kunta)

    oppijaService
      .getOppijalista(oppivelvollisetKoskessa.map(_.masterOid))
      .map(oppivelvollisetKoskessa => {
        val eiSuorittavat =
          oppivelvollisetKoskessa
            .map(ValpasRouhintaOppivelvollinen.apply)
            .filterNot(_.suorittaaOppivelvollisuutta)

        KuntarouhinnanTulos(
          eiOppivelvollisuuttaSuorittavat = eiSuorittavat,
        )
      })
  }
}

case class KuntarouhinnanTulos(
  eiOppivelvollisuuttaSuorittavat: Seq[ValpasRouhintaOppivelvollinen],
)
