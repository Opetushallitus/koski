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

    def samaDiaarinumeroAmmatillinen(a: AmmatillinenOpiskeluoikeus) = {
      def dn = (oo: Opiskeluoikeus) => oo.suoritukset
        .collectFirst { case s: AmmatillisenTutkinnonSuoritus => s }
        .flatMap(s => s.koulutusmoduuli.perusteenDiaarinumero)
      opiskeluoikeus match {
        case x: AmmatillinenOpiskeluoikeus => dn(x) == dn(a)
        case _ => false
      }
    }

    def päällekkäinenAikajakso(oo: Opiskeluoikeus) = {
      Aikajakso(opiskeluoikeus.alkamispäivä, opiskeluoikeus.päättymispäivä)
        .overlaps(Aikajakso(oo.alkamispäivä, oo.päättymispäivä))
    }

    def findPäällekkäinenAikajakso(vertailtavatOot: Seq[Opiskeluoikeus]): Option[Opiskeluoikeus] = {
      vertailtavatOot.find(päällekkäinenAikajakso)
    }

    def findSamaOppilaitosJaTyyppiSamaanAikaan(): Either[HttpStatus, Option[Opiskeluoikeus]] = {
      oppijanMuutOpiskeluoikeudetSamaOppilaitosJaTyyppi.map(findPäällekkäinenAikajakso)
    }

    def findConflictingAikuistenPerusopetus(): Either[HttpStatus, Option[Opiskeluoikeus]] = {
      val vertailtavatOot: Either[HttpStatus, Seq[Opiskeluoikeus]] = oppijanMuutOpiskeluoikeudetSamaOppilaitosJaTyyppi.map(_
        .filter(sisältääAikuistenPerusopetuksenOppimääränSuorituksen(_) == sisältääAikuistenPerusopetuksenOppimääränSuorituksen(opiskeluoikeus))
      )

      if (sisältääAikuistenPerusopetuksenOppimääränSuorituksen(opiskeluoikeus)) {
        vertailtavatOot.map(findPäällekkäinenAikajakso)
      } else {
        vertailtavatOot.map(_.find(_.päättymispäivä.isEmpty))
      }
    }

    def findConflictingPerusopetus(): Either[HttpStatus, Option[Opiskeluoikeus]] = {
      val vertailtavatOot: Either[HttpStatus, Seq[Opiskeluoikeus]] = oppijanMuutOpiskeluoikeudetSamaOppilaitosJaTyyppi.map(_
        .filter(sisältääNuortenPerusopetuksenOppimääränTaiVuosiluokanSuorituksen(_) == sisältääNuortenPerusopetuksenOppimääränTaiVuosiluokanSuorituksen(opiskeluoikeus))
      )

      if (sisältääNuortenPerusopetuksenOppimääränTaiVuosiluokanSuorituksen(opiskeluoikeus)) {
        // Oppimäärän opinnot, vain aikajaksoltaan kokonaan erillisiä saa duplikoida
        vertailtavatOot.map(findPäällekkäinenAikajakso)
      } else {
        // aineopinnot, vain päättyneitä saa duplikoida
        vertailtavatOot.map(_.find(_.päättymispäivä.isEmpty))
      }
    }

    def findConflictingAmmatillinen(): Either[HttpStatus, Option[Opiskeluoikeus]] = {
      // sama diaarinumero ja päällekkäinen aikajakso mutta ei päättynyt
      oppijanMuutOpiskeluoikeudetSamaOppilaitosJaTyyppi.map(_.find {
        case a: AmmatillinenOpiskeluoikeus if !a.tila.opiskeluoikeusjaksot.last.opiskeluoikeusPäättynyt => samaDiaarinumeroAmmatillinen(a) && päällekkäinenAikajakso(a)
        case _ => false
      })
    }

    def throwIfConflictingExists(
      isConflicting: () => Either[HttpStatus, Option[Opiskeluoikeus]],
      oo: KoskeenTallennettavaOpiskeluoikeus,
      ignoreInProd: Boolean = false): HttpStatus = {
      def logWarning(conflicting: Opiskeluoikeus): Unit = logger.warn(s"Opiskeluoikeus jäisi kiinni duplikaattivalidaatioihin (${oo.getClass}, konfliktoiva: ${conflicting.oid})")
      isConflicting() match {
        // Tuotantokäyttöönoton yhteydessä siivoa pois aiemmat validaatiot: vastaavanRinnakkaisenOpiskeluoikeudenLisääminenSallittu
        case Right(Some(conflicting)) if Environment.isProdEnvironment(config) && ignoreInProd =>
          logWarning(conflicting)
          HttpStatus.ok
        case Right(Some(conflicting)) =>
          logWarning(conflicting)
          KoskiErrorCategory.conflict.exists()
        case Right(None) => HttpStatus.ok
        case Left(error) => error
      }
    }

    opiskeluoikeus match {
      case oo: EsiopetuksenOpiskeluoikeus => throwIfConflictingExists(findSamaOppilaitosJaTyyppiSamaanAikaan, oo, ignoreInProd = true)
      case oo: PerusopetukseenValmistavanOpetuksenOpiskeluoikeus => throwIfConflictingExists(findSamaOppilaitosJaTyyppiSamaanAikaan, oo, ignoreInProd = true)
      case oo: AikuistenPerusopetuksenOpiskeluoikeus => throwIfConflictingExists(findConflictingAikuistenPerusopetus, oo, ignoreInProd = true)
      case oo: PerusopetuksenOpiskeluoikeus => throwIfConflictingExists(findConflictingPerusopetus, oo)
      case oo: AmmatillinenOpiskeluoikeus if !isMuuAmmatillinenOpiskeluoikeus => throwIfConflictingExists(findConflictingAmmatillinen, oo)
      case _ => HttpStatus.ok
    }
  }

}
