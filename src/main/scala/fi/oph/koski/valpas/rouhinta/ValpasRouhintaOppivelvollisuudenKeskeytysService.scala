package fi.oph.koski.valpas.rouhinta

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.log.Logging
import fi.oph.koski.valpas.db.ValpasSchema.OppivelvollisuudenKeskeytysRow
import fi.oph.koski.valpas.valpasrepository.ValpasOppivelvollisuudenKeskeytys

class ValpasRouhintaOppivelvollisuudenKeskeytysService(application: KoskiApplication) extends ValpasRouhintaTiming {
  private val ovKeskeytysRepository = application.valpasOppivelvollisuudenKeskeytysRepository
  private val rajapäivätService = application.valpasRajapäivätService

  def fetchOppivelvollisuudenKeskeytykset(
    oppijat: Seq[ValpasRouhintaOppivelvollinen]
  ): Seq[ValpasRouhintaOppivelvollinen] =
  {
    rouhintaTimed("fetchOppivelvollisuudenKeskeytykset", oppijat.size) {
      lazy val virhePuuttuvistaOppijaOideista =
        "Yritettiin hakea oppivelvollisuuden keskeytyksiä ilman listaa oppijan kaikista oideista."

      val kaikkiOppijaOidit = oppijat
        .flatMap(
          _.kaikkiOidit.getOrElse(
            throw new InternalError(virhePuuttuvistaOppijaOideista)
          )
        )

      val keskeytykset: Map[String, Seq[OppivelvollisuudenKeskeytysRow]] =
        ovKeskeytysRepository
          .getKeskeytykset(kaikkiOppijaOidit)
          .groupBy(_.oppijaOid)
          .withDefaultValue(Seq.empty)

      oppijat.map(oppija => oppija.copy(
        oppivelvollisuudenKeskeytys =
          oppija.kaikkiOidit.getOrElse(throw new InternalError(virhePuuttuvistaOppijaOideista))
            .flatMap(keskeytykset)
            .map(ValpasOppivelvollisuudenKeskeytys.apply(rajapäivätService.tarkastelupäivä))
      ))
    }
  }

}
