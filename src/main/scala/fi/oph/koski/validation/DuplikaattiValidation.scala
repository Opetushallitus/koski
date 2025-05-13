package fi.oph.koski.validation

import com.typesafe.config.Config
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.CompositeOpiskeluoikeusRepository
import fi.oph.koski.schema._
import fi.oph.koski.validation.PerusopetuksenOpiskeluoikeusValidation.{sisältääAikuistenPerusopetuksenOppimääränSuorituksen, sisältääNuortenPerusopetuksenOppimääränTaiVuosiluokanSuorituksen}

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

    lazy val isLukionOppimäärä: Boolean = opiskeluoikeus.suoritukset.forall {
      case _: LukionOppimääränSuoritus2015 | _: LukionOppimääränSuoritus2019 => true
      case _ => false
    }

    lazy val isJotpa: Boolean =
      opiskeluoikeus.suoritukset.forall {
        case _: VapaanSivistystyönJotpaKoulutuksenSuoritus => true
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

    lazy val aiemminTallennettuOpiskeluoikeus = oppijanOpiskeluoikeudet.map(_.find(samaOo(opiskeluoikeus, _)))

    def samaDiaarinumeroAmmatillinen(a: AmmatillinenOpiskeluoikeus): Boolean = {
      def diaarinumerot = (oo: Opiskeluoikeus) => oo.suoritukset
        .collect { case s: AmmatillisenTutkinnonSuoritus => s }
        .flatMap(s => s.koulutusmoduuli.perusteenDiaarinumero)

      opiskeluoikeus match {
        case x: AmmatillinenOpiskeluoikeus => diaarinumerot(x).intersect(diaarinumerot(a)).nonEmpty
        case _ => false
      }
    }

    def päällekkäinenAikajakso(oo: Opiskeluoikeus): Boolean = {
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
      // sama diaarinumero ammatillisen tutkinnon suorituksilla ja päällekkäinen aikajakso mutta ei päättynyt
      oppijanMuutOpiskeluoikeudetSamaOppilaitosJaTyyppi.map(_.find {
        case a: AmmatillinenOpiskeluoikeus if !a.tila.opiskeluoikeusjaksot.last.opiskeluoikeusPäättynyt => samaDiaarinumeroAmmatillinen(a) && päällekkäinenAikajakso(a)
        case _ => false
      })
    }

    def findConflictingOpiskeluoikeus(): Either[HttpStatus, Option[Opiskeluoikeus]] = {
      oppijanMuutOpiskeluoikeudetSamaOppilaitosJaTyyppi.map(_.find {
        case o: Opiskeluoikeus if !o.tila.opiskeluoikeusjaksot.last.opiskeluoikeusPäättynyt => true
        case _ => false
      })
    }

    def findNuortenPerusopetuksessaUseitaKeskeneräisiäVuosiluokanSuorituksia(): Either[HttpStatus, Option[Opiskeluoikeus]] = {
      def getLuokkaAste(s: Suoritus) = s.koulutusmoduuli.tunniste.koodiarvo
      def getVuosiluokat(oo: Opiskeluoikeus) = oo.suoritukset.collect { case s: PerusopetuksenVuosiluokanSuoritus => s }
      def getSuoritetutLuokkaAsteet(oo: Opiskeluoikeus): List[String] = getVuosiluokat(oo).filterNot(_.kesken).map(getLuokkaAste)
      def getKeskeneräisetLuokkaAsteet(oo: Opiskeluoikeus): List[String] = getVuosiluokat(oo).filter(_.kesken).map(getLuokkaAste)

      aiemminTallennettuOpiskeluoikeus match {
        case Right(Some(aiempi)) if aiempi.oid == opiskeluoikeus.oid =>
          val aiemminTallennetutKeskeneräisetLuokkaAsteet = getKeskeneräisetLuokkaAsteet(aiempi)
          val uudetkeskeneräisetLuokkaAsteet = getKeskeneräisetLuokkaAsteet(opiskeluoikeus)
          val suoritetutLuokkaAsteet = getSuoritetutLuokkaAsteet(opiskeluoikeus)

          val keskentilaisetVuosiluokanSuoritukset =
            (aiemminTallennetutKeskeneräisetLuokkaAsteet.diff(suoritetutLuokkaAsteet) ++ uudetkeskeneräisetLuokkaAsteet).distinct

          if (keskentilaisetVuosiluokanSuoritukset.size > 1) {
            val luokat = keskentilaisetVuosiluokanSuoritukset.mkString(" ja ")
            val message = s"Nuorten perusopetuksen opiskeluoikeudessa ei saa olla kuin enintään yksi kesken-tilainen vuosiluokan suoritus. Siirron jälkeen kesken olisivat perusopetuksen luokka-asteet $luokat."
            Left(KoskiErrorCategory.badRequest.validation.tila.useitaKeskeneräisiäVuosiluokanSuoritukia(message))
          } else {
            Right(None)
          }
        case _ => Right(None)
      }
    }

    def throwIfConflictingExists(
      isConflicting: () => Either[HttpStatus, Option[Opiskeluoikeus]]
    ): HttpStatus = {
      isConflicting() match {
        case Right(Some(_)) =>
          KoskiErrorCategory.conflict.exists()
        case Right(None) => HttpStatus.ok
        case Left(error) => error
      }
    }

    opiskeluoikeus match {
      case _: EsiopetuksenOpiskeluoikeus => throwIfConflictingExists(findSamaOppilaitosJaTyyppiSamaanAikaan)
      case _: PerusopetukseenValmistavanOpetuksenOpiskeluoikeus => throwIfConflictingExists(findSamaOppilaitosJaTyyppiSamaanAikaan)
      case _: AikuistenPerusopetuksenOpiskeluoikeus => throwIfConflictingExists(findConflictingAikuistenPerusopetus)
      case _: PerusopetuksenOpiskeluoikeus =>
        HttpStatus.fold(
          throwIfConflictingExists(findConflictingPerusopetus),
          throwIfConflictingExists(findNuortenPerusopetuksessaUseitaKeskeneräisiäVuosiluokanSuorituksia),
        )
      case _: AmmatillinenOpiskeluoikeus if isMuuAmmatillinenOpiskeluoikeus => HttpStatus.ok
      case _: AmmatillinenOpiskeluoikeus => throwIfConflictingExists(findConflictingAmmatillinen)
      case _: LukionOpiskeluoikeus if !isLukionOppimäärä => HttpStatus.ok
      case _: LukionOpiskeluoikeus if isLukionOppimäärä => throwIfConflictingExists(findSamaOppilaitosJaTyyppiSamaanAikaan)
      case _: VapaanSivistystyönOpiskeluoikeus if isJotpa => HttpStatus.ok
      case _: VapaanSivistystyönOpiskeluoikeus => throwIfConflictingExists(findConflictingOpiskeluoikeus)
      case _: MuunKuinSäännellynKoulutuksenOpiskeluoikeus => HttpStatus.ok
      case _: TaiteenPerusopetuksenOpiskeluoikeus => HttpStatus.ok
      case _: Opiskeluoikeus => throwIfConflictingExists(findConflictingOpiskeluoikeus)
    }
  }

}
