package fi.oph.koski.validation

import com.typesafe.config.Config
import fi.oph.koski.config.Environment
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.CompositeOpiskeluoikeusRepository
import fi.oph.koski.schema.{Aikajakso, AikuistenPerusopetuksenOpiskeluoikeus, AmmatillinenOpiskeluoikeus, AmmatillisenTutkinnonSuoritus, EsiopetuksenOpiskeluoikeus, KoskeenTallennettavaOpiskeluoikeus, MuunAmmatillisenKoulutuksenSuoritus, Opiskeluoikeus, PerusopetukseenValmistavanOpetuksenOpiskeluoikeus, PerusopetuksenOpiskeluoikeus}
import fi.oph.koski.validation.PerusopetuksenOpiskeluoikeusValidation.{logger, sisältääAikuistenPerusopetuksenOppimääränSuorituksen, sisältääNuortenPerusopetuksenOppimääränTaiVuosiluokanSuorituksen}

object DuplikaattiValidation extends Logging {
  def samaOo(oo1: Opiskeluoikeus, oo2: Opiskeluoikeus): Boolean = {
    val samaOid = oo2.oid.isDefined && oo2.oid == oo1.oid
    val samaLähdejärjestelmänId = oo2.lähdejärjestelmänId.isDefined && oo2.lähdejärjestelmänId == oo1.lähdejärjestelmänId
    samaOid || samaLähdejärjestelmänId
  }

  def validateDuplikaatit(
     opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
     oppijanHenkilötiedot: LaajatOppijaHenkilöTiedot,
     opiskeluoikeusRepository: CompositeOpiskeluoikeusRepository,
     config: Config
   ): HttpStatus = {

    lazy val isMuuAmmatillinenOpiskeluoikeus: Boolean =
      opiskeluoikeus.suoritukset.forall {
        case _: MuunAmmatillisenKoulutuksenSuoritus => true
        case _ => false
      }

    lazy val oppijanOpiskeluoikeudet = opiskeluoikeusRepository.findByOppija(
        tunnisteet = oppijanHenkilötiedot,
        useVirta = false,
        useYtr = false
      )(KoskiSpecificSession.systemUser)
      .warningsToLeft

    lazy val oppijanMuutOpiskeluoikeudetSamaOppilaitosJaTyyppi = oppijanOpiskeluoikeudet
      .map(_
        .filterNot(samaOo(opiskeluoikeus, _))
        .filter(_.oppilaitos.map(_.oid) == opiskeluoikeus.oppilaitos.map(_.oid))
        .filter(_.tyyppi == opiskeluoikeus.tyyppi)
      )

    def päällekkäinenAikajakso(oo: Opiskeluoikeus) = {
      Aikajakso(opiskeluoikeus.alkamispäivä, opiskeluoikeus.päättymispäivä)
        .overlaps(Aikajakso(oo.alkamispäivä, oo.päättymispäivä))
    }


    def jollainPäällekkäinenAikajakso(vertailtavatOot: Seq[Opiskeluoikeus]): Boolean = {
      vertailtavatOot.exists(päällekkäinenAikajakso)
    }

    def samaDiaarinumeroAmmatillinen(a: AmmatillinenOpiskeluoikeus) = {
      def dn = (oo: Opiskeluoikeus) => oo.suoritukset
        .collectFirst { case s: AmmatillisenTutkinnonSuoritus => s }
        .flatMap(s => s.koulutusmoduuli.perusteenDiaarinumero)
      opiskeluoikeus match {
        case x: AmmatillinenOpiskeluoikeus => dn(x) == dn(a)
        case _ => false
      }
    }

    def oppijallaOnDuplikaatti(): Either[HttpStatus, Boolean] = {
      oppijanMuutOpiskeluoikeudetSamaOppilaitosJaTyyppi.map(jollainPäällekkäinenAikajakso)
    }

    def oppijallaOnDuplikaattiAikuistenPerusopetus(): Either[HttpStatus, Boolean] = {
      val vertailtavatOot: Either[HttpStatus, Seq[Opiskeluoikeus]] = oppijanMuutOpiskeluoikeudetSamaOppilaitosJaTyyppi.map(_
        .filter(sisältääAikuistenPerusopetuksenOppimääränSuorituksen(_) == sisältääAikuistenPerusopetuksenOppimääränSuorituksen(opiskeluoikeus))
      )

      if (sisältääAikuistenPerusopetuksenOppimääränSuorituksen(opiskeluoikeus)) {
        vertailtavatOot.map(jollainPäällekkäinenAikajakso)
      } else {
        vertailtavatOot.map(_.exists(_.päättymispäivä.isEmpty))
      }
    }

    def oppijallaOnDuplikaattiPerusopetus(): Either[HttpStatus, Boolean] = {
      val vertailtavatOot: Either[HttpStatus, Seq[Opiskeluoikeus]] = oppijanMuutOpiskeluoikeudetSamaOppilaitosJaTyyppi.map(_
        .filter(sisältääNuortenPerusopetuksenOppimääränTaiVuosiluokanSuorituksen(_) == sisältääNuortenPerusopetuksenOppimääränTaiVuosiluokanSuorituksen(opiskeluoikeus))
      )

      if (sisältääNuortenPerusopetuksenOppimääränTaiVuosiluokanSuorituksen(opiskeluoikeus)) {
        // Oppimäärän opinnot, vain aikajaksoltaan kokonaan erillisiä saa duplikoida
        vertailtavatOot.map(jollainPäällekkäinenAikajakso)
      } else {
        // aineopinnot, vain päättyneitä saa duplikoida
        vertailtavatOot.map(_.exists(_.päättymispäivä.isEmpty))
      }
    }

    def oppijallaOnDuplikaattiAmmatillinen(): Either[HttpStatus, Boolean] = {
      oppijanMuutOpiskeluoikeudetSamaOppilaitosJaTyyppi.map(oot => oot.exists {
        case a: AmmatillinenOpiskeluoikeus if !a.tila.opiskeluoikeusjaksot.last.opiskeluoikeusPäättynyt => samaDiaarinumeroAmmatillinen(a) && päällekkäinenAikajakso(a)
        case _ => false
      })
    }

    def throwConflictExists(
      isConflicting: () => Either[HttpStatus, Boolean],
      oo: KoskeenTallennettavaOpiskeluoikeus,
      ignoreInProd: Boolean = false): HttpStatus =
    {
      isConflicting() match {
        case Right(true) if Environment.isProdEnvironment(config) && ignoreInProd =>
          logger.warn(s"Opiskeluoikeus ${oo.oid} jäisi kiinni duplikaattivalidaatioihin")
          HttpStatus.ok
        case Right(true) => KoskiErrorCategory.conflict.exists()
        case Right(false) => HttpStatus.ok
        case Left(error) => error
      }
    }

    opiskeluoikeus match {
      case oo: EsiopetuksenOpiskeluoikeus => throwConflictExists(oppijallaOnDuplikaatti, oo, ignoreInProd = true)
      case oo: PerusopetukseenValmistavanOpetuksenOpiskeluoikeus => throwConflictExists(oppijallaOnDuplikaatti, oo, ignoreInProd = true)
      case oo: AikuistenPerusopetuksenOpiskeluoikeus => throwConflictExists(oppijallaOnDuplikaattiAikuistenPerusopetus, oo, ignoreInProd = true)
      case oo: PerusopetuksenOpiskeluoikeus => throwConflictExists(oppijallaOnDuplikaattiPerusopetus, oo)
      case oo: AmmatillinenOpiskeluoikeus if !isMuuAmmatillinenOpiskeluoikeus => throwConflictExists(oppijallaOnDuplikaattiAmmatillinen, oo)
      case _ => HttpStatus.ok
    }
  }

}
